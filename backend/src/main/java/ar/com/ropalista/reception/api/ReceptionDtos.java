package ar.com.ropalista.reception.api;

import ar.com.ropalista.reception.domain.ReceptionApprovalStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ReceptionDtos {
    private ReceptionDtos() {}

    public record CreateReceptionRequest(
            OffsetDateTime receivedAt,
            @NotNull @Positive Integer actualWeightGrams,
            @Size(max = 2000) String conditionNotes,
            @Size(max = 100) String bagCode,
            @NotEmpty List<@Valid ReceptionItemRequest> items,
            List<@Valid EvidenceRequest> evidences
    ) {}

    public record ReceptionItemRequest(
            @NotBlank @Size(max = 60) String equivalenceCode,
            @PositiveOrZero int actualPhysicalPieces,
            boolean damageDetected,
            boolean stainDetected,
            @Size(max = 1000) String observations
    ) {}

    public record EvidenceRequest(
            @NotBlank @Size(max = 500) String objectKey,
            @NotBlank @Size(max = 255) String fileName,
            @NotBlank @Size(max = 120) String contentType,
            @Positive long sizeBytes,
            @NotBlank @Pattern(regexp = "[0-9A-Fa-f]{64}") String sha256,
            @Size(max = 500) String caption
    ) {}

    public record DecisionRequest(
            @NotNull ReceptionApprovalStatus decision,
            @Size(max = 1000) String notes
    ) {}

    public record ReceptionResponse(
            UUID id,
            UUID orderId,
            String idempotencyKey,
            OffsetDateTime receivedAt,
            int declaredPhysicalPieces,
            int actualPhysicalPieces,
            Integer declaredWeightGrams,
            int actualWeightGrams,
            int pieceDifference,
            Integer weightDifferenceGrams,
            String conditionNotes,
            boolean damageDetected,
            boolean stainDetected,
            boolean requiresCustomerApproval,
            ReceptionApprovalStatus approvalStatus,
            OffsetDateTime approvalAt,
            String approvalBy,
            String approvalNotes,
            String labelCode,
            String bagCode,
            String orderStatus,
            List<ReceptionItemResponse> items,
            List<EvidenceResponse> evidences
    ) {}

    public record ReceptionItemResponse(
            String equivalenceCode,
            String equivalenceName,
            int declaredPhysicalPieces,
            int actualPhysicalPieces,
            int pieceDifference,
            boolean damageDetected,
            boolean stainDetected,
            String observations
    ) {}

    public record EvidenceResponse(
            UUID id,
            String objectKey,
            String fileName,
            String contentType,
            long sizeBytes,
            String sha256,
            String caption
    ) {}
}
