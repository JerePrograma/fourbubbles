package ar.com.ropalista.compatibility.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.compatibility.api.CompatibilityDtos;
import ar.com.ropalista.compatibility.domain.CompatibilityEvaluation;
import ar.com.ropalista.compatibility.domain.CompatibilityException;
import ar.com.ropalista.compatibility.domain.OrderTreatmentProfile;
import ar.com.ropalista.compatibility.persistence.CompatibilityEvaluationRepository;
import ar.com.ropalista.compatibility.persistence.OrderTreatmentProfileRepository;
import ar.com.ropalista.order.domain.LaundryOrder;
import ar.com.ropalista.order.domain.OrderStatus;
import ar.com.ropalista.order.persistence.LaundryOrderRepository;
import ar.com.ropalista.reception.persistence.OrderReceptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CompatibilityService {
    private final OrderTreatmentProfileRepository profiles;
    private final CompatibilityEvaluationRepository evaluations;
    private final LaundryOrderRepository orders;
    private final OrderReceptionRepository receptions;
    private final CompatibilityEngine engine;
    private final ObjectMapper objectMapper;
    private final AuditService audit;

    public CompatibilityService(OrderTreatmentProfileRepository profiles,
                                CompatibilityEvaluationRepository evaluations,
                                LaundryOrderRepository orders,
                                OrderReceptionRepository receptions,
                                CompatibilityEngine engine,
                                ObjectMapper objectMapper,
                                AuditService audit) {
        this.profiles = profiles;
        this.evaluations = evaluations;
        this.orders = orders;
        this.receptions = receptions;
        this.engine = engine;
        this.objectMapper = objectMapper;
        this.audit = audit;
    }

    @Transactional
    public CompatibilityDtos.TreatmentProfileResponse saveProfile(
            UUID orderId, CompatibilityDtos.TreatmentProfileRequest request) {
        LaundryOrder order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND));
        if (order.getStatus() != OrderStatus.CLASSIFIED) {
            throw new BusinessException("ORDER_NOT_READY_FOR_COMPATIBILITY",
                    "El perfil de tratamiento solo puede editarse en CLASSIFIED",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var reception = receptions.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException("RECEPTION_NOT_FOUND",
                        "El pedido debe poseer una recepción", HttpStatus.UNPROCESSABLE_ENTITY));
        OrderTreatmentProfile profile = profiles.findByOrderId(orderId).orElse(null);
        Map<String, Object> before = profile == null ? null : profileSummary(profile);
        if (profile == null) {
            profile = new OrderTreatmentProfile(order, reception, request.colorGroup(), request.materialGroup(),
                    request.maxTemperatureC(), request.dryerAllowed(), request.fragrancePolicy(),
                    request.softenerAllowed(), request.hypoallergenic(), request.babyClothes(),
                    request.petContact(), request.heavySoil(), request.exclusiveCycle(), request.notes());
        } else {
            profile.update(request.colorGroup(), request.materialGroup(), request.maxTemperatureC(),
                    request.dryerAllowed(), request.fragrancePolicy(), request.softenerAllowed(),
                    request.hypoallergenic(), request.babyClothes(), request.petContact(),
                    request.heavySoil(), request.exclusiveCycle(), request.notes());
        }
        OrderTreatmentProfile saved = profiles.saveAndFlush(profile);
        audit.record("TREATMENT_PROFILE", saved.getId(), before == null ? "CREATE" : "UPDATE",
                before, profileSummary(saved), "Perfil de tratamiento para compatibilidad");
        return toProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public CompatibilityDtos.TreatmentProfileResponse getProfile(UUID orderId) {
        if (orders.findByIdAndDeletedAtIsNull(orderId).isEmpty()) {
            throw new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND);
        }
        return profiles.findByOrderId(orderId).map(this::toProfileResponse).orElse(null);
    }

    @Transactional
    public CompatibilityDtos.EvaluationResponse evaluate(CompatibilityDtos.EvaluateRequest request) {
        if (request.orderAId().equals(request.orderBId())) {
            throw new BusinessException("SAME_ORDER_COMPATIBILITY",
                    "Debe seleccionar dos pedidos diferentes", HttpStatus.BAD_REQUEST);
        }
        UUID firstId = request.orderAId().compareTo(request.orderBId()) < 0
                ? request.orderAId() : request.orderBId();
        UUID secondId = firstId.equals(request.orderAId()) ? request.orderBId() : request.orderAId();
        OrderTreatmentProfile first = profiles.findByOrderId(firstId)
                .orElseThrow(() -> missingProfile(firstId));
        OrderTreatmentProfile second = profiles.findByOrderId(secondId)
                .orElseThrow(() -> missingProfile(secondId));
        requireClassified(first.getOrder());
        requireClassified(second.getOrder());

        Specification<CompatibilityEvaluation> specification = (root, query, builder) -> builder.and(
                builder.equal(root.get("orderA").get("id"), firstId),
                builder.equal(root.get("orderB").get("id"), secondId),
                builder.equal(root.get("profileAVersion"), first.getVersion()),
                builder.equal(root.get("profileBVersion"), second.getVersion()),
                builder.equal(root.get("ruleVersion"), CompatibilityEngine.RULE_VERSION));
        var existing = evaluations.findOne(specification);
        if (existing.isPresent()) {
            return toEvaluationResponse(existing.get());
        }

        var result = engine.evaluate(toProfileData(first), toProfileData(second));
        CompatibilityEvaluation evaluation = new CompatibilityEvaluation(first.getOrder(), second.getOrder(),
                first.getVersion(), second.getVersion(), CompatibilityEngine.RULE_VERSION,
                result.compatible(), json(result.reasons()), json(result.recommendation()));
        CompatibilityEvaluation saved = evaluations.saveAndFlush(evaluation);
        audit.record("COMPATIBILITY_EVALUATION", saved.getId(), "CREATE", null,
                Map.of("orderAId", firstId, "orderBId", secondId,
                        "compatible", saved.isCompatible(), "ruleVersion", saved.getRuleVersion()),
                "Evaluación explicable de compatibilidad");
        return toEvaluationResponse(saved);
    }

    @Transactional(readOnly = true)
    public CompatibilityDtos.EvaluationResponse getEvaluation(UUID evaluationId) {
        return toEvaluationResponse(evaluations.findById(evaluationId)
                .orElseThrow(() -> new BusinessException("COMPATIBILITY_EVALUATION_NOT_FOUND",
                        "Evaluación inexistente", HttpStatus.NOT_FOUND)));
    }

    @Transactional
    public CompatibilityDtos.EvaluationResponse authorizeException(
            UUID evaluationId, CompatibilityDtos.ExceptionRequest request, String actor) {
        CompatibilityEvaluation evaluation = evaluations.findById(evaluationId)
                .orElseThrow(() -> new BusinessException("COMPATIBILITY_EVALUATION_NOT_FOUND",
                        "Evaluación inexistente", HttpStatus.NOT_FOUND));
        if (evaluation.isCompatible()) {
            throw new BusinessException("COMPATIBILITY_EXCEPTION_NOT_REQUIRED",
                    "Una evaluación compatible no requiere excepción", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (evaluation.getException() != null) {
            throw new BusinessException("COMPATIBILITY_EXCEPTION_ALREADY_EXISTS",
                    "La evaluación ya posee una excepción", HttpStatus.CONFLICT);
        }
        CompatibilityException exception = new CompatibilityException(
                request.reason(), actor, OffsetDateTime.now());
        evaluation.authorizeException(exception);
        CompatibilityEvaluation saved = evaluations.saveAndFlush(evaluation);
        audit.record("COMPATIBILITY_EVALUATION", saved.getId(), "AUTHORIZE_EXCEPTION",
                Map.of("effectiveCompatible", false),
                Map.of("effectiveCompatible", true, "authorizedBy", actor),
                request.reason());
        return toEvaluationResponse(saved);
    }

    private void requireClassified(LaundryOrder order) {
        if (order.getStatus() != OrderStatus.CLASSIFIED) {
            throw new BusinessException("ORDER_NOT_READY_FOR_COMPATIBILITY",
                    "Ambos pedidos deben estar en CLASSIFIED", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private BusinessException missingProfile(UUID orderId) {
        return new BusinessException("TREATMENT_PROFILE_NOT_FOUND",
                "Falta el perfil de tratamiento del pedido " + orderId,
                HttpStatus.UNPROCESSABLE_ENTITY);
    }

    private CompatibilityEngine.ProfileData toProfileData(OrderTreatmentProfile profile) {
        return new CompatibilityEngine.ProfileData(profile.getColorGroup(), profile.getMaterialGroup(),
                profile.getMaxTemperatureC(), profile.isDryerAllowed(), profile.getFragrancePolicy(),
                profile.isSoftenerAllowed(), profile.isHypoallergenic(), profile.isBabyClothes(),
                profile.isPetContact(), profile.isHeavySoil(), profile.isExclusiveCycle());
    }

    private CompatibilityDtos.TreatmentProfileResponse toProfileResponse(OrderTreatmentProfile profile) {
        return new CompatibilityDtos.TreatmentProfileResponse(profile.getId(), profile.getOrder().getId(),
                profile.getReception().getId(), profile.getVersion(), profile.getColorGroup(),
                profile.getMaterialGroup(), profile.getMaxTemperatureC(), profile.isDryerAllowed(),
                profile.getFragrancePolicy(), profile.isSoftenerAllowed(), profile.isHypoallergenic(),
                profile.isBabyClothes(), profile.isPetContact(), profile.isHeavySoil(),
                profile.isExclusiveCycle(), profile.getNotes());
    }

    private CompatibilityDtos.EvaluationResponse toEvaluationResponse(CompatibilityEvaluation evaluation) {
        List<CompatibilityEngine.Reason> reasons = readReasons(evaluation.getReasons());
        CompatibilityEngine.Recommendation recommendation = readRecommendation(evaluation.getRecommendation());
        CompatibilityDtos.ExceptionResponse exception = evaluation.getException() == null ? null
                : new CompatibilityDtos.ExceptionResponse(evaluation.getException().getId(),
                        evaluation.getException().getReason(), evaluation.getException().getAuthorizedBy(),
                        evaluation.getException().getAuthorizedAt());
        return new CompatibilityDtos.EvaluationResponse(evaluation.getId(), evaluation.getOrderA().getId(),
                evaluation.getOrderB().getId(), evaluation.getProfileAVersion(), evaluation.getProfileBVersion(),
                evaluation.getRuleVersion(), evaluation.isCompatible(), exception != null,
                evaluation.isEffectivelyCompatible(), reasons, recommendation, exception);
    }

    private Map<String, Object> profileSummary(OrderTreatmentProfile profile) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("orderId", profile.getOrder().getId());
        value.put("version", profile.getVersion());
        value.put("colorGroup", profile.getColorGroup());
        value.put("materialGroup", profile.getMaterialGroup());
        value.put("maxTemperatureC", profile.getMaxTemperatureC());
        value.put("hypoallergenic", profile.isHypoallergenic());
        value.put("babyClothes", profile.isBabyClothes());
        value.put("petContact", profile.isPetContact());
        value.put("heavySoil", profile.isHeavySoil());
        value.put("exclusiveCycle", profile.isExclusiveCycle());
        return value;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo persistir la evaluación de compatibilidad", ex);
        }
    }

    private List<CompatibilityEngine.Reason> readReasons(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Razones de compatibilidad inválidas", ex);
        }
    }

    private CompatibilityEngine.Recommendation readRecommendation(String json) {
        try {
            return objectMapper.readValue(json, CompatibilityEngine.Recommendation.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Recomendación de compatibilidad inválida", ex);
        }
    }
}
