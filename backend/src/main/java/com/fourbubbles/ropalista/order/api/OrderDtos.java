package com.fourbubbles.ropalista.order.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {
    private OrderDtos() {}

    public record CreateOrderRequest(
            @NotNull UUID customerId,
            @NotNull UUID addressId,
            @NotNull UUID servicePlanId,
            @NotBlank @Size(max = 30) String modality,
            boolean exclusiveCycle,
            @Min(0) Integer declaredWeightGrams,
            Instant pickupScheduledAt,
            Instant promisedAt,
            @Size(max = 2000) String notes
    ) {}

    public record ReceiveOrderRequest(
            @NotNull @Min(1) Integer actualWeightGrams,
            @NotEmpty List<@Valid ItemRequest> items,
            @Size(max = 2000) String observation
    ) {}

    public record ItemRequest(
            @NotNull UUID garmentEquivalenceId,
            @Min(1) int physicalPieceCount,
            @Size(max = 1000) String observations
    ) {}

    public record QuoteRequest(@Size(max = 50) String promotionCode) {}

    public record TransitionRequest(
            @NotNull String newStatus,
            @Size(max = 2000) String observation,
            @Size(max = 200) String location,
            @Size(max = 200) String notificationReference
    ) {}

    public record OrderResponse(
            UUID id,
            String orderNumber,
            UUID customerId,
            String customerName,
            UUID addressId,
            UUID servicePlanId,
            String serviceName,
            String status,
            String modality,
            boolean exclusiveCycle,
            int physicalPieceCount,
            BigDecimal equivalentUnits,
            Integer declaredWeightGrams,
            Integer actualWeightGrams,
            BigDecimal quotedAmount,
            BigDecimal confirmedAmount,
            String currency,
            String pricingExplanation,
            Instant pickupScheduledAt,
            Instant promisedAt,
            Instant deliveredAt,
            List<ItemResponse> items
    ) {}

    public record ItemResponse(
            UUID id,
            UUID garmentEquivalenceId,
            String name,
            int physicalPieceCount,
            int groupCount,
            BigDecimal equivalentUnits,
            String observations
    ) {}

    public record PriceQuoteResponse(
            BigDecimal baseAmount,
            BigDecimal discountAmount,
            BigDecimal creditAmount,
            BigDecimal finalAmount,
            String currency,
            String promotionCode,
            String explanation
    ) {}
}
