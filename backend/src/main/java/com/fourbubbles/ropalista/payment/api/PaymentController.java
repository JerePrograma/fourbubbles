package com.fourbubbles.ropalista.payment.api;

import com.fourbubbles.ropalista.audit.application.AuditService;
import com.fourbubbles.ropalista.common.api.ApiResponse;
import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import com.fourbubbles.ropalista.common.application.ResourceNotFoundException;
import com.fourbubbles.ropalista.order.domain.LaundryOrder;
import com.fourbubbles.ropalista.order.infrastructure.LaundryOrderRepository;
import com.fourbubbles.ropalista.payment.domain.Payment;
import com.fourbubbles.ropalista.payment.domain.PaymentStatus;
import com.fourbubbles.ropalista.payment.infrastructure.PaymentRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders/{orderId}/payments")
@PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
public class PaymentController {
    private final LaundryOrderRepository orders;
    private final PaymentRepository payments;
    private final AuditService audit;

    public PaymentController(LaundryOrderRepository orders, PaymentRepository payments, AuditService audit) {
        this.orders = orders;
        this.payments = payments;
        this.audit = audit;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ApiResponse<PaymentResponse> register(@PathVariable UUID orderId,
            @Valid @RequestBody RegisterPaymentRequest request) {
        LaundryOrder order = orders.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", orderId));
        if (order.getConfirmedAmount() == null) {
            throw new BusinessRuleException("PRICE_NOT_CONFIRMED",
                    "No puede registrarse un pago antes de confirmar el precio");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setCustomer(order.getCustomer());
        payment.setAmount(request.amount());
        payment.setCurrency(order.getCurrency());
        payment.setPaidAt(request.paidAt() == null ? Instant.now() : request.paidAt());
        payment.setMethodCode(request.methodCode().trim().toUpperCase());
        payment.setReference(request.reference());
        BigDecimal before = payments.totalPaid(orderId);
        BigDecimal after = before.add(request.amount());
        payment.setStatus(after.compareTo(order.getConfirmedAmount()) >= 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL);
        payments.save(payment);
        audit.record("Payment", payment.getId(), "CREATE", before.toPlainString(), after.toPlainString(),
                "Pago de pedido " + order.getOrderNumber());

        return ApiResponse.of(new PaymentResponse(payment.getId(), payment.getAmount(), payment.getCurrency(),
                payment.getPaidAt(), payment.getMethodCode(), payment.getReference(), payment.getStatus().name(),
                order.getConfirmedAmount().subtract(after).max(BigDecimal.ZERO)));
    }

    public record RegisterPaymentRequest(
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            Instant paidAt,
            @NotBlank @Size(max = 40) String methodCode,
            @Size(max = 200) String reference
    ) {}

    public record PaymentResponse(UUID id, BigDecimal amount, String currency, Instant paidAt,
                                  String methodCode, String reference, String status, BigDecimal remainingBalance) {}
}
