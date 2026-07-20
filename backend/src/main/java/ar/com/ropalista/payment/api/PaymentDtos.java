package ar.com.ropalista.payment.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class PaymentDtos {
    private PaymentDtos() {}

    public record RegisterPaymentRequest(@NotNull UUID orderId, @NotBlank String methodCode,
                                         @NotNull @Positive BigDecimal amount, OffsetDateTime paidAt,
                                         @Size(max = 160) String reference,
                                         @Size(max = 500) String notes) {}

    public record PaymentResponse(UUID id, UUID orderId, String methodCode, BigDecimal amount,
                                  String currencyCode, OffsetDateTime paidAt, String reference,
                                  BigDecimal totalPaid, BigDecimal remainingBalance, String orderPaymentStatus) {}

    public record PaymentHistoryResponse(UUID id, UUID orderId, String methodCode, String methodName,
                                         BigDecimal amount, String currencyCode, OffsetDateTime paidAt,
                                         String reference, String notes, String status, String registeredBy) {}
}
