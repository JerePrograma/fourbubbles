package ar.com.ropalista.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {
    private OrderDtos() {}

    public record CreateOrderRequest(
            @NotNull UUID clientId,
            @NotNull UUID addressId,
            @NotBlank String serviceCode,
            String promotionCode,
            @Positive Integer declaredWeightGrams,
            boolean exclusiveCycle,
            OffsetDateTime pickupScheduledAt,
            OffsetDateTime promisedAt,
            @Size(max = 2000) String notes,
            @NotEmpty List<@Valid ItemRequest> items
    ) {}

    public record ItemRequest(@NotBlank String equivalenceCode, @Positive int physicalPieces,
                              @Size(max = 500) String observations) {}

    public record ChangeStatusRequest(@NotNull String newStatus, @Size(max = 1000) String observation,
                                      @Size(max = 200) String location,
                                      @Size(max = 200) String notificationReference) {}

    public record OrderResponse(UUID id, String orderNumber, UUID clientId, UUID addressId, String serviceCode,
                                String status, String paymentStatus, int physicalPieces, BigDecimal equivalentUnits,
                                Integer declaredWeightGrams, Integer actualWeightGrams, boolean exclusiveCycle,
                                boolean requiresQuote, String limitReached, BigDecimal quotedPrice,
                                BigDecimal confirmedPrice, String currencyCode, String priceBreakdown,
                                OffsetDateTime pickupScheduledAt, OffsetDateTime promisedAt,
                                List<ItemResponse> items) {}

    public record ItemResponse(String equivalenceCode, String name, int physicalPieces, int groups,
                               BigDecimal equivalentUnits, Integer estimatedWeightGrams, String observations) {}
}
