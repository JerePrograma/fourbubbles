package ar.com.ropalista.production.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.production.api.ProductionSeparationDtos;
import ar.com.ropalista.production.domain.ProductionCycle;
import ar.com.ropalista.production.domain.ProductionCycleOrder;
import ar.com.ropalista.production.domain.ProductionCycleStatus;
import ar.com.ropalista.production.persistence.ProductionCycleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ProductionSeparationService {
    private final ProductionCycleRepository cycles;
    private final AuditService audit;

    public ProductionSeparationService(ProductionCycleRepository cycles, AuditService audit) {
        this.cycles = cycles;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<ProductionSeparationDtos.SeparationResponse> list(UUID cycleId) {
        ProductionCycle cycle = cycles.findDetailedById(cycleId)
                .orElseThrow(() -> notFound("PRODUCTION_CYCLE_NOT_FOUND", "Ciclo inexistente"));
        return cycle.getOrders().stream()
                .filter(ProductionCycleOrder::isSeparationRequired)
                .sorted(java.util.Comparator.comparingInt(ProductionCycleOrder::getAssignmentOrder))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductionSeparationDtos.SeparationResponse confirm(
            UUID cycleId,
            UUID orderId,
            ProductionSeparationDtos.ConfirmSeparationRequest request,
            String actor) {
        ProductionCycle cycle = cycles.findByIdForUpdate(cycleId)
                .orElseThrow(() -> notFound("PRODUCTION_CYCLE_NOT_FOUND", "Ciclo inexistente"));
        if (cycle.getStatus() != ProductionCycleStatus.PLANNED) {
            throw conflict("PRODUCTION_CYCLE_ALREADY_STARTED",
                    "La separación solo puede confirmarse antes de iniciar el ciclo");
        }
        ProductionCycleOrder assignment = cycle.getOrders().stream()
                .filter(value -> value.getOrder().getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> notFound("PRODUCTION_CYCLE_ORDER_NOT_FOUND",
                        "El pedido no pertenece al ciclo"));
        if (!assignment.isSeparationRequired()) {
            throw unprocessable("SEPARATION_NOT_REQUIRED",
                    "El pedido no requiere separación física en este ciclo");
        }

        String containerCode = normalizeContainerCode(request.containerCode());
        boolean duplicated = cycle.getOrders().stream()
                .filter(value -> value != assignment)
                .map(ProductionCycleOrder::getSeparationContainerCode)
                .filter(value -> value != null)
                .anyMatch(value -> value.equalsIgnoreCase(containerCode));
        if (duplicated) {
            throw conflict("SEPARATION_CONTAINER_ALREADY_USED",
                    "El contenedor ya está asignado a otro pedido del ciclo");
        }

        Map<String, Object> before = summary(assignment);
        try {
            assignment.confirmSeparation(containerCode, actor, OffsetDateTime.now());
        } catch (IllegalStateException ex) {
            throw conflict("SEPARATION_ALREADY_CONFIRMED", ex.getMessage());
        }
        audit.record("PRODUCTION_CYCLE_ORDER", assignment.getId(), "CONFIRM_SEPARATION",
                before, summary(assignment), "Confirmación física previa al ciclo");
        return toResponse(assignment);
    }

    private ProductionSeparationDtos.SeparationResponse toResponse(ProductionCycleOrder assignment) {
        return new ProductionSeparationDtos.SeparationResponse(
                assignment.getCycle().getId(),
                assignment.getCycle().getCycleNumber(),
                assignment.getOrder().getId(),
                assignment.getOrder().getOrderNumber(),
                assignment.isSeparationRequired(),
                assignment.getSeparationContainerCode(),
                assignment.getSeparationConfirmedAt(),
                assignment.getSeparationConfirmedBy());
    }

    private Map<String, Object> summary(ProductionCycleOrder assignment) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("cycleId", assignment.getCycle().getId());
        value.put("orderId", assignment.getOrder().getId());
        value.put("separationRequired", assignment.isSeparationRequired());
        value.put("containerCode", assignment.getSeparationContainerCode());
        value.put("confirmedAt", assignment.getSeparationConfirmedAt());
        value.put("confirmedBy", assignment.getSeparationConfirmedBy());
        return value;
    }

    private String normalizeContainerCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }

    private BusinessException conflict(String code, String message) {
        return new BusinessException(code, message, HttpStatus.CONFLICT);
    }

    private BusinessException unprocessable(String code, String message) {
        return new BusinessException(code, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
