package ar.com.ropalista.reception.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.catalog.domain.GarmentEquivalence;
import ar.com.ropalista.catalog.persistence.GarmentEquivalenceRepository;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.order.application.OrderTransitionPolicy;
import ar.com.ropalista.order.domain.LaundryOrder;
import ar.com.ropalista.order.domain.OrderItem;
import ar.com.ropalista.order.domain.OrderStateHistory;
import ar.com.ropalista.order.domain.OrderStatus;
import ar.com.ropalista.order.persistence.LaundryOrderRepository;
import ar.com.ropalista.order.persistence.OrderStateHistoryRepository;
import ar.com.ropalista.reception.api.ReceptionDtos;
import ar.com.ropalista.reception.domain.OrderReception;
import ar.com.ropalista.reception.domain.ReceptionApprovalStatus;
import ar.com.ropalista.reception.domain.ReceptionEvidence;
import ar.com.ropalista.reception.domain.ReceptionItem;
import ar.com.ropalista.reception.persistence.OrderReceptionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ReceptionService {
    private static final Pattern IDEMPOTENCY_KEY = Pattern.compile("[A-Za-z0-9._:-]{16,120}");

    private final OrderReceptionRepository receptions;
    private final LaundryOrderRepository orders;
    private final OrderStateHistoryRepository histories;
    private final GarmentEquivalenceRepository equivalences;
    private final OrderTransitionPolicy transitionPolicy;
    private final ReceptionDifferencePolicy differencePolicy;
    private final AuditService audit;
    private final JdbcTemplate jdbcTemplate;

    public ReceptionService(OrderReceptionRepository receptions, LaundryOrderRepository orders,
                            OrderStateHistoryRepository histories, GarmentEquivalenceRepository equivalences,
                            OrderTransitionPolicy transitionPolicy, ReceptionDifferencePolicy differencePolicy,
                            AuditService audit, JdbcTemplate jdbcTemplate) {
        this.receptions = receptions;
        this.orders = orders;
        this.histories = histories;
        this.equivalences = equivalences;
        this.transitionPolicy = transitionPolicy;
        this.differencePolicy = differencePolicy;
        this.audit = audit;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ReceptionDtos.ReceptionResponse receive(UUID orderId, String rawIdempotencyKey,
                                                    ReceptionDtos.CreateReceptionRequest request) {
        String idempotencyKey = normalizeIdempotencyKey(rawIdempotencyKey);
        var earlyExisting = receptions.findByIdempotencyKey(idempotencyKey);
        if (earlyExisting.isPresent()) {
            return sameOrderOrConflict(earlyExisting.get(), orderId);
        }

        LaundryOrder order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND));

        var existingForOrder = receptions.findByOrderId(orderId);
        if (existingForOrder.isPresent()) {
            OrderReception existing = existingForOrder.get();
            if (existing.getIdempotencyKey().equals(idempotencyKey)) {
                return toResponse(existing);
            }
            throw new BusinessException("ORDER_ALREADY_RECEIVED",
                    "El pedido ya posee una recepción registrada", HttpStatus.CONFLICT);
        }
        var existingForKey = receptions.findByIdempotencyKey(idempotencyKey);
        if (existingForKey.isPresent()) {
            return sameOrderOrConflict(existingForKey.get(), orderId);
        }
        if (order.getStatus() != OrderStatus.PICKED_UP) {
            throw new BusinessException("ORDER_NOT_READY_FOR_RECEPTION",
                    "La recepción solo puede registrarse desde PICKED_UP", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        OffsetDateTime receivedAt = request.receivedAt() == null ? OffsetDateTime.now() : request.receivedAt();
        if (receivedAt.isAfter(OffsetDateTime.now().plusMinutes(5))) {
            throw new BusinessException("INVALID_RECEPTION_TIME",
                    "La fecha de recepción no puede estar en el futuro", HttpStatus.BAD_REQUEST);
        }

        Map<String, OrderItem> declaredByCode = order.getItems().stream()
                .collect(Collectors.toMap(
                        item -> normalizeCode(item.getEquivalence().getCode()),
                        Function.identity(),
                        (first, second) -> first,
                        LinkedHashMap::new));
        Map<String, Integer> declaredPieces = order.getItems().stream()
                .collect(Collectors.groupingBy(
                        item -> normalizeCode(item.getEquivalence().getCode()),
                        LinkedHashMap::new,
                        Collectors.summingInt(OrderItem::getPhysicalPieces)));

        Map<String, ReceptionDtos.ReceptionItemRequest> requestedByCode = new LinkedHashMap<>();
        for (var item : request.items()) {
            String code = normalizeCode(item.equivalenceCode());
            if (requestedByCode.putIfAbsent(code, item) != null) {
                throw new BusinessException("DUPLICATE_RECEPTION_ITEM",
                        "La recepción contiene el código repetido: " + code, HttpStatus.BAD_REQUEST);
            }
        }
        Set<String> missingCodes = declaredPieces.keySet().stream()
                .filter(code -> !requestedByCode.containsKey(code))
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (!missingCodes.isEmpty()) {
            throw new BusinessException("MISSING_DECLARED_RECEPTION_ITEMS",
                    "Faltan prendas declaradas en recepción: " + String.join(", ", missingCodes),
                    HttpStatus.BAD_REQUEST);
        }

        List<ReceptionItem> receptionItems = new ArrayList<>();
        int actualPieces = 0;
        boolean damageDetected = false;
        boolean stainDetected = false;
        for (var entry : requestedByCode.entrySet()) {
            String code = entry.getKey();
            var itemRequest = entry.getValue();
            OrderItem declared = declaredByCode.get(code);
            GarmentEquivalence equivalence;
            String name;
            int declaredCount = declaredPieces.getOrDefault(code, 0);
            if (declared != null) {
                equivalence = declared.getEquivalence();
                name = equivalence.getName();
            } else {
                equivalence = equivalences.findActive(code, LocalDate.now())
                        .orElseThrow(() -> new BusinessException("EQUIVALENCE_NOT_FOUND",
                                "Equivalencia no vigente: " + code, HttpStatus.UNPROCESSABLE_ENTITY));
                name = equivalence.getName();
            }
            actualPieces = Math.addExact(actualPieces, itemRequest.actualPhysicalPieces());
            damageDetected |= itemRequest.damageDetected();
            stainDetected |= itemRequest.stainDetected();
            receptionItems.add(new ReceptionItem(equivalence, code, name, declaredCount,
                    itemRequest.actualPhysicalPieces(), itemRequest.damageDetected(),
                    itemRequest.stainDetected(), itemRequest.observations()));
        }
        if (actualPieces <= 0) {
            throw new BusinessException("EMPTY_RECEPTION",
                    "La recepción debe contener al menos una pieza real", HttpStatus.BAD_REQUEST);
        }

        var evaluation = differencePolicy.evaluate(order.getPhysicalPieces(), actualPieces,
                order.getDeclaredWeightGrams(), request.actualWeightGrams(), damageDetected);
        String labelCode = nextLabelCode();
        OrderReception reception = new OrderReception(order, idempotencyKey, receivedAt,
                order.getPhysicalPieces(), actualPieces, order.getDeclaredWeightGrams(),
                request.actualWeightGrams(), request.conditionNotes(), damageDetected, stainDetected,
                evaluation.requiresCustomerApproval(), labelCode, request.bagCode());
        receptionItems.forEach(reception::addItem);
        if (request.evidences() != null) {
            request.evidences().forEach(evidence -> reception.addEvidence(new ReceptionEvidence(
                    evidence.objectKey(), evidence.fileName(), evidence.contentType(), evidence.sizeBytes(),
                    evidence.sha256(), evidence.caption())));
        }

        order.recordReception(actualPieces, request.actualWeightGrams());
        transition(order, OrderStatus.RECEIVED, "Recepción física registrada");
        transition(order, OrderStatus.PENDING_INSPECTION, "Inspección de recepción registrada");
        transition(order, evaluation.requiresCustomerApproval()
                        ? OrderStatus.WAITING_PRICE_APPROVAL
                        : OrderStatus.CLASSIFIED,
                evaluation.requiresCustomerApproval()
                        ? "Diferencias de recepción pendientes de aprobación"
                        : "Recepción sin diferencias que requieran aprobación");

        OrderReception saved = receptions.saveAndFlush(reception);
        Map<String, Object> auditValue = new LinkedHashMap<>();
        auditValue.put("orderId", order.getId());
        auditValue.put("actualPhysicalPieces", actualPieces);
        auditValue.put("actualWeightGrams", request.actualWeightGrams());
        auditValue.put("pieceDifference", evaluation.pieceDifference());
        auditValue.put("weightDifferenceGrams", evaluation.weightDifferenceGrams());
        auditValue.put("approvalStatus", saved.getApprovalStatus());
        auditValue.put("labelCode", labelCode);
        audit.record("ORDER_RECEPTION", saved.getId(), "CREATE", null, auditValue,
                "Recepción idempotente del pedido");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ReceptionDtos.ReceptionResponse get(UUID orderId) {
        if (!orders.existsById(orderId)) {
            throw new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND);
        }
        return receptions.findByOrderId(orderId).map(this::toResponse).orElse(null);
    }

    @Transactional
    public ReceptionDtos.ReceptionResponse decide(UUID orderId, ReceptionDtos.DecisionRequest request,
                                                   String actor) {
        LaundryOrder order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND));
        OrderReception reception = receptions.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("RECEPTION_NOT_FOUND",
                        "El pedido no posee una recepción", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.WAITING_PRICE_APPROVAL
                || reception.getApprovalStatus() != ReceptionApprovalStatus.PENDING) {
            throw new BusinessException("RECEPTION_DECISION_NOT_ALLOWED",
                    "La recepción no tiene una decisión pendiente", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (request.decision() != ReceptionApprovalStatus.APPROVED
                && request.decision() != ReceptionApprovalStatus.REJECTED) {
            throw new BusinessException("INVALID_RECEPTION_DECISION",
                    "La decisión debe ser APPROVED o REJECTED", HttpStatus.BAD_REQUEST);
        }
        ReceptionApprovalStatus previous = reception.getApprovalStatus();
        reception.decide(request.decision(), actor, OffsetDateTime.now(), request.notes());
        transition(order,
                request.decision() == ReceptionApprovalStatus.APPROVED
                        ? OrderStatus.CLASSIFIED
                        : OrderStatus.CANCELLED,
                request.notes());
        audit.record("ORDER_RECEPTION", reception.getId(), "DECIDE",
                Map.of("approvalStatus", previous),
                Map.of("approvalStatus", reception.getApprovalStatus(), "orderStatus", order.getStatus()),
                request.notes());
        return toResponse(reception);
    }

    private ReceptionDtos.ReceptionResponse sameOrderOrConflict(OrderReception reception, UUID orderId) {
        if (!reception.getOrder().getId().equals(orderId)) {
            throw new BusinessException("IDEMPOTENCY_KEY_CONFLICT",
                    "La clave de idempotencia ya fue utilizada para otro pedido", HttpStatus.CONFLICT);
        }
        return toResponse(reception);
    }

    private void transition(LaundryOrder order, OrderStatus target, String observation) {
        OrderStatus previous = order.getStatus();
        if (!transitionPolicy.canTransition(previous, target)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "No se permite pasar de " + previous + " a " + target,
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        order.changeStatus(target);
        histories.save(new OrderStateHistory(order, previous, target, observation, null, null));
    }

    private ReceptionDtos.ReceptionResponse toResponse(OrderReception reception) {
        List<ReceptionDtos.ReceptionItemResponse> items = reception.getItems().stream()
                .sorted(Comparator.comparing(ReceptionItem::getEquivalenceCodeSnapshot))
                .map(item -> new ReceptionDtos.ReceptionItemResponse(
                        item.getEquivalenceCodeSnapshot(), item.getEquivalenceNameSnapshot(),
                        item.getDeclaredPhysicalPieces(), item.getActualPhysicalPieces(),
                        item.getPieceDifference(), item.isDamageDetected(), item.isStainDetected(),
                        item.getObservations()))
                .toList();
        List<ReceptionDtos.EvidenceResponse> evidences = reception.getEvidences().stream()
                .map(evidence -> new ReceptionDtos.EvidenceResponse(evidence.getId(), evidence.getObjectKey(),
                        evidence.getFileName(), evidence.getContentType(), evidence.getSizeBytes(),
                        evidence.getSha256(), evidence.getCaption()))
                .toList();
        return new ReceptionDtos.ReceptionResponse(reception.getId(), reception.getOrder().getId(),
                reception.getIdempotencyKey(), reception.getReceivedAt(), reception.getDeclaredPhysicalPieces(),
                reception.getActualPhysicalPieces(), reception.getDeclaredWeightGrams(),
                reception.getActualWeightGrams(), reception.getPieceDifference(),
                reception.getWeightDifferenceGrams(), reception.getConditionNotes(),
                reception.isDamageDetected(), reception.isStainDetected(),
                reception.isRequiresCustomerApproval(), reception.getApprovalStatus(),
                reception.getApprovalAt(), reception.getApprovalBy(), reception.getApprovalNotes(),
                reception.getLabelCode(), reception.getBagCode(), reception.getOrder().getStatus().name(),
                items, evidences);
    }

    private String normalizeIdempotencyKey(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("IDEMPOTENCY_KEY_REQUIRED",
                    "Debe informar la cabecera Idempotency-Key", HttpStatus.BAD_REQUEST);
        }
        String normalized = raw.trim();
        if (!IDEMPOTENCY_KEY.matcher(normalized).matches()) {
            throw new BusinessException("INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key debe contener entre 16 y 120 caracteres seguros",
                    HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String nextLabelCode() {
        Long value = jdbcTemplate.queryForObject("select nextval('reception_label_seq')", Long.class);
        return "RCV-%06d".formatted(value);
    }
}
