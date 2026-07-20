package ar.com.ropalista.order.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.catalog.persistence.GarmentEquivalenceRepository;
import ar.com.ropalista.catalog.persistence.ServiceOfferingRepository;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.customer.persistence.AddressRepository;
import ar.com.ropalista.customer.persistence.ClientRepository;
import ar.com.ropalista.order.api.OrderDtos;
import ar.com.ropalista.order.domain.LaundryOrder;
import ar.com.ropalista.order.domain.OrderItem;
import ar.com.ropalista.order.domain.OrderStateHistory;
import ar.com.ropalista.order.domain.OrderStatus;
import ar.com.ropalista.order.persistence.LaundryOrderRepository;
import ar.com.ropalista.order.persistence.OrderStateHistoryRepository;
import ar.com.ropalista.pricing.application.PricingService;
import ar.com.ropalista.pricing.domain.PromotionUsage;
import ar.com.ropalista.pricing.persistence.PromotionUsageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

@Service
public class OrderService {
    private final LaundryOrderRepository orders;
    private final OrderStateHistoryRepository histories;
    private final ClientRepository clients;
    private final AddressRepository addresses;
    private final ServiceOfferingRepository services;
    private final GarmentEquivalenceRepository equivalences;
    private final GarmentEquivalenceCalculator equivalenceCalculator;
    private final OrderLimitPolicy limitPolicy;
    private final OrderTransitionPolicy transitionPolicy;
    private final PricingService pricingService;
    private final PromotionUsageRepository promotionUsages;
    private final AuditService audit;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public OrderService(LaundryOrderRepository orders, OrderStateHistoryRepository histories,
                        ClientRepository clients, AddressRepository addresses,
                        ServiceOfferingRepository services, GarmentEquivalenceRepository equivalences,
                        GarmentEquivalenceCalculator equivalenceCalculator, OrderLimitPolicy limitPolicy,
                        OrderTransitionPolicy transitionPolicy, PricingService pricingService,
                        PromotionUsageRepository promotionUsages, AuditService audit,
                        ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.orders = orders;
        this.histories = histories;
        this.clients = clients;
        this.addresses = addresses;
        this.services = services;
        this.equivalences = equivalences;
        this.equivalenceCalculator = equivalenceCalculator;
        this.limitPolicy = limitPolicy;
        this.transitionPolicy = transitionPolicy;
        this.pricingService = pricingService;
        this.promotionUsages = promotionUsages;
        this.audit = audit;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public OrderDtos.OrderResponse create(OrderDtos.CreateOrderRequest request) {
        var client = clients.findByIdAndDeletedAtIsNull(request.clientId())
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND", "Cliente inexistente", HttpStatus.NOT_FOUND));
        var address = addresses.findByIdAndClientIdAndActiveTrue(request.addressId(), client.getId())
                .orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", "Domicilio inexistente o ajeno al cliente", HttpStatus.NOT_FOUND));
        ServiceOffering service = services.findActive(request.serviceCode(), LocalDate.now())
                .orElseThrow(() -> new BusinessException("SERVICE_NOT_FOUND", "Servicio inexistente o sin vigencia", HttpStatus.UNPROCESSABLE_ENTITY));

        var calculatedItems = new ArrayList<CalculatedItem>();
        BigDecimal equivalentTotal = BigDecimal.ZERO;
        int physicalTotal = 0;
        boolean requiresQuote = service.isRequiresQuote();
        boolean exclusive = request.exclusiveCycle();
        for (var itemRequest : request.items()) {
            var rule = equivalences.findActive(itemRequest.equivalenceCode(), LocalDate.now())
                    .orElseThrow(() -> new BusinessException("EQUIVALENCE_NOT_FOUND", "Equivalencia no vigente: " + itemRequest.equivalenceCode(), HttpStatus.UNPROCESSABLE_ENTITY));
            var calculation = equivalenceCalculator.calculate(rule, itemRequest.physicalPieces());
            physicalTotal = Math.addExact(physicalTotal, calculation.physicalPieces());
            equivalentTotal = equivalentTotal.add(calculation.equivalentUnits());
            requiresQuote |= calculation.quoteRequired();
            exclusive |= calculation.exclusiveCycleRequired();
            calculatedItems.add(new CalculatedItem(rule, calculation, itemRequest.observations()));
        }

        var limit = limitPolicy.evaluate(service, equivalentTotal, request.declaredWeightGrams());
        if (limit.splitOrDifferentServiceRequired()) {
            requiresQuote = true;
        }
        boolean firstOrder = !orders.existsByClientIdAndDeletedAtIsNull(client.getId());
        var priceQuote = pricingService.quote(service, client, address, request.promotionCode(), firstOrder, OffsetDateTime.now());
        String orderNumber = nextOrderNumber();
        LaundryOrder order = new LaundryOrder(orderNumber, client, address, service,
                priceQuote.priceDefinition(), priceQuote.promotion(), physicalTotal, equivalentTotal,
                request.declaredWeightGrams(), exclusive, requiresQuote, limit.firstLimitReached(),
                priceQuote.total(), priceQuote.currency(), json(priceQuote.breakdown()),
                request.pickupScheduledAt(), request.promisedAt(), request.notes());
        calculatedItems.forEach(item -> order.addItem(new OrderItem(item.rule(), item.calculation().physicalPieces(),
                item.calculation().groups(), item.calculation().equivalentUnits(),
                item.calculation().estimatedWeightGrams(), item.observations())));
        LaundryOrder saved = orders.save(order);
        histories.save(new OrderStateHistory(saved, null, saved.getStatus(), "Creación del pedido", null, null));
        audit.record("ORDER", saved.getId(), "CREATE", null,
                java.util.Map.of("orderNumber", saved.getOrderNumber(), "status", saved.getStatus(), "quotedPrice", saved.getQuotedPrice()),
                "Alta y cotización del pedido");
        return toResponse(saved);
    }

    @Transactional
    public OrderDtos.OrderResponse confirmPrice(UUID id) {
        LaundryOrder order = find(id);
        if (order.isRequiresQuote()) {
            throw new BusinessException("MANUAL_QUOTE_REQUIRED", "El pedido requiere presupuesto manual antes de confirmar", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        order.confirmPrice();
        if (order.getPromotion() != null && !promotionUsages.existsByOrderId(order.getId())) {
            promotionUsages.save(new PromotionUsage(order.getId(), order.getPromotion(), order.getClient(), order.getAddress()));
        }
        if (order.getStatus() == OrderStatus.QUOTED) {
            changeStatusInternal(order, OrderStatus.WAITING_CONFIRMATION, "Precio confirmado", null, null);
        }
        audit.record("ORDER", order.getId(), "CONFIRM_PRICE", null,
                java.util.Map.of("confirmedPrice", order.getConfirmedPrice()), "Confirmación de precio histórico");
        return toResponse(order);
    }

    @Transactional
    public OrderDtos.OrderResponse changeStatus(UUID id, OrderDtos.ChangeStatusRequest request) {
        LaundryOrder order = find(id);
        OrderStatus target;
        try {
            target = OrderStatus.valueOf(request.newStatus());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_ORDER_STATUS", "Estado de pedido inválido", HttpStatus.BAD_REQUEST);
        }
        changeStatusInternal(order, target, request.observation(), request.location(), request.notificationReference());
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Page<OrderDtos.OrderSummaryResponse> search(String orderNumber, UUID clientId, OrderStatus status,
                                                        int page, int size) {
        Specification<LaundryOrder> specification = (root, query, builder) -> builder.isNull(root.get("deletedAt"));
        if (orderNumber != null && !orderNumber.isBlank()) {
            String pattern = "%" + orderNumber.trim().toUpperCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) ->
                    builder.like(builder.upper(root.get("orderNumber")), pattern));
        }
        if (clientId != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("client").get("id"), clientId));
        }
        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return orders.findAll(specification, pageable).map(this::toSummary);
    }

    private void changeStatusInternal(LaundryOrder order, OrderStatus target, String observation,
                                      String location, String notificationReference) {
        OrderStatus previous = order.getStatus();
        if (!transitionPolicy.canTransition(previous, target)) {
            throw new BusinessException("INVALID_STATUS_TRANSITION",
                    "No se permite pasar de " + previous + " a " + target, HttpStatus.UNPROCESSABLE_ENTITY);
        }
        order.changeStatus(target);
        histories.save(new OrderStateHistory(order, previous, target, observation, location, notificationReference));
        audit.record("ORDER", order.getId(), "STATUS_CHANGE", java.util.Map.of("status", previous),
                java.util.Map.of("status", target), observation);
    }

    private LaundryOrder find(UUID id) {
        return orders.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND));
    }

    private String nextOrderNumber() {
        Long value = jdbcTemplate.queryForObject("select nextval('order_number_seq')", Long.class);
        return "RL-%06d".formatted(value);
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo persistir el detalle del precio", ex);
        }
    }

    private OrderDtos.OrderSummaryResponse toSummary(LaundryOrder order) {
        String clientName = order.getClient().getLastName() + ", " + order.getClient().getFirstName();
        return new OrderDtos.OrderSummaryResponse(order.getId(), order.getOrderNumber(), order.getClient().getId(),
                clientName, order.getService().getCode(), order.getService().getName(), order.getStatus().name(),
                order.getPaymentStatus().name(), order.getPhysicalPieces(), order.getEquivalentUnits(),
                order.getQuotedPrice(), order.getConfirmedPrice(), order.getCurrencyCode(),
                order.getPickupScheduledAt(), order.getPromisedAt(), order.getCreatedAt());
    }

    private OrderDtos.OrderResponse toResponse(LaundryOrder order) {
        var allowedTransitions = transitionPolicy.allowedTransitions(order.getStatus()).stream()
                .sorted()
                .map(Enum::name)
                .toList();
        return new OrderDtos.OrderResponse(order.getId(), order.getOrderNumber(), order.getClient().getId(),
                order.getAddress().getId(), order.getService().getCode(), order.getStatus().name(),
                order.getPaymentStatus().name(), order.getPhysicalPieces(), order.getEquivalentUnits(),
                order.getDeclaredWeightGrams(), order.getActualWeightGrams(), order.isExclusiveCycle(),
                order.isRequiresQuote(), order.getLimitReached(), order.getQuotedPrice(), order.getConfirmedPrice(),
                order.getCurrencyCode(), order.getPriceBreakdown(), order.getPickupScheduledAt(), order.getPromisedAt(),
                allowedTransitions,
                order.getItems().stream().map(item -> new OrderDtos.ItemResponse(item.getEquivalence().getCode(),
                        item.getEquivalence().getName(), item.getPhysicalPieces(), item.getGroupCount(),
                        item.getEquivalentUnitsApplied(), item.getEstimatedWeightGrams(), item.getObservations())).toList());
    }

    private record CalculatedItem(ar.com.ropalista.catalog.domain.GarmentEquivalence rule,
                                  GarmentEquivalenceCalculator.Calculation calculation,
                                  String observations) {}
}
