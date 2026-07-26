package ar.com.ropalista.production.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class ProductionSeparationDtos {
    private ProductionSeparationDtos() {}

    public record ConfirmSeparationRequest(
            @NotBlank
            @Pattern(regexp = "[A-Za-z0-9._:-]{3,80}",
                    message = "containerCode debe contener entre 3 y 80 caracteres seguros")
            String containerCode) {}

    public record SeparationResponse(
            UUID cycleId,
            String cycleNumber,
            UUID orderId,
            String orderNumber,
            boolean separationRequired,
            String containerCode,
            OffsetDateTime confirmedAt,
            String confirmedBy) {}
}
