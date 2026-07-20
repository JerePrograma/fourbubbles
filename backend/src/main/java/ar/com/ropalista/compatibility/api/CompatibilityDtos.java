package ar.com.ropalista.compatibility.api;

import ar.com.ropalista.compatibility.application.CompatibilityEngine;
import ar.com.ropalista.compatibility.domain.ColorGroup;
import ar.com.ropalista.compatibility.domain.FragrancePolicy;
import ar.com.ropalista.compatibility.domain.MaterialGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class CompatibilityDtos {
    private CompatibilityDtos() {}

    public record TreatmentProfileRequest(
            @NotNull ColorGroup colorGroup,
            @NotNull MaterialGroup materialGroup,
            @Min(20) @Max(95) int maxTemperatureC,
            boolean dryerAllowed,
            @NotNull FragrancePolicy fragrancePolicy,
            boolean softenerAllowed,
            boolean hypoallergenic,
            boolean babyClothes,
            boolean petContact,
            boolean heavySoil,
            boolean exclusiveCycle,
            @Size(max = 1000) String notes
    ) {}

    public record TreatmentProfileResponse(
            UUID id,
            UUID orderId,
            UUID receptionId,
            long version,
            ColorGroup colorGroup,
            MaterialGroup materialGroup,
            int maxTemperatureC,
            boolean dryerAllowed,
            FragrancePolicy fragrancePolicy,
            boolean softenerAllowed,
            boolean hypoallergenic,
            boolean babyClothes,
            boolean petContact,
            boolean heavySoil,
            boolean exclusiveCycle,
            String notes
    ) {}

    public record EvaluateRequest(@NotNull UUID orderAId, @NotNull UUID orderBId) {}

    public record ExceptionRequest(@NotBlank @Size(max = 1000) String reason) {}

    public record EvaluationResponse(
            UUID id,
            UUID orderAId,
            UUID orderBId,
            long profileAVersion,
            long profileBVersion,
            String ruleVersion,
            boolean compatible,
            boolean overridden,
            boolean effectivelyCompatible,
            List<CompatibilityEngine.Reason> reasons,
            CompatibilityEngine.Recommendation recommendation,
            ExceptionResponse exception
    ) {}

    public record ExceptionResponse(
            UUID id,
            String reason,
            String authorizedBy,
            OffsetDateTime authorizedAt
    ) {}
}
