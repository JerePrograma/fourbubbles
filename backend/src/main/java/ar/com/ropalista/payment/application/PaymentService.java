package ar.com.ropalista.payment.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.order.domain.PaymentStatus;
import ar.com.ropalista.order.persistence.LaundryOrderRepository;
import ar.com.ropalista.payment.api.PaymentDtos;
import ar.com.ropalista.payment.domain.Payment;
import ar.com.ropalista.payment.persistence.PaymentMethodRepository;
import ar.com.ropalista.payment.persistence.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    private final PaymentRepository payments;
    private final PaymentMethodRepository methods;
    private final LaundryOrderRepository orders;
    private final AuditService audit;

    public PaymentService(PaymentRepository payments, PaymentMethodRepository methods,
                          LaundryOrderRepository orders, AuditService audit) {
        this.payments = payments;
        this.methods = methods;
        this.orders = orders;
        this.audit = audit;
    }

    @Transactional
    public PaymentDtos.PaymentResponse register(PaymentDtos.RegisterPaymentRequest request) {
        var order = orders.findByIdForUpdate(request.orderId())
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND));
        if (order.getConfirmedPrice() == null) {
            throw new BusinessException("PRICE_NOT_CONFIRMED", "No se puede cobrar un pedido sin precio confirmado", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var method = methods.findByCodeAndActiveTrue(request.methodCode())
                .orElseThrow(() -> new BusinessException("PAYMENT_METHOD_NOT_FOUND", "Medio de pago no habilitado", HttpStatus.UNPROCESSABLE_ENTITY));
        BigDecimal previousPaid = money(payments.sumPaidByOrderId(order.getId()));
        BigDecimal newPaid = previousPaid.add(money(request.amount()));
        if (newPaid.compareTo(order.getConfirmedPrice()) > 0) {
            throw new BusinessException("PAYMENT_EXCEEDS_BALANCE", "El pago supera el saldo del pedido", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Payment payment = payments.save(new Payment(order, method, money(request.amount()), order.getCurrencyCode(),
                request.paidAt() == null ? OffsetDateTime.now() : request.paidAt(), request.reference(), request.notes()));
        BigDecimal remaining = money(order.getConfirmedPrice().subtract(newPaid));
        order.updatePaymentStatus(remaining.signum() == 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL);
        audit.record("PAYMENT", payment.getId(), "REGISTER", null,
                java.util.Map.of("orderId", order.getId(), "amount", payment.getAmount(), "method", method.getCode()),
                "Registro de pago");
        return new PaymentDtos.PaymentResponse(payment.getId(), order.getId(), method.getCode(), payment.getAmount(),
                payment.getCurrencyCode(), payment.getPaidAt(), payment.getReference(), money(newPaid), remaining,
                order.getPaymentStatus().name());
    }

    @Transactional(readOnly = true)
    public List<PaymentDtos.PaymentHistoryResponse> history(UUID orderId) {
        orders.findByIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new BusinessException("ORDER_NOT_FOUND", "Pedido inexistente", HttpStatus.NOT_FOUND));
        return payments.findByOrderIdOrderByPaidAtAsc(orderId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private PaymentDtos.PaymentHistoryResponse toHistoryResponse(Payment payment) {
        return new PaymentDtos.PaymentHistoryResponse(payment.getId(), payment.getOrder().getId(),
                payment.getMethod().getCode(), payment.getMethod().getName(), payment.getAmount(),
                payment.getCurrencyCode(), payment.getPaidAt(), payment.getReference(), payment.getNotes(),
                payment.getStatus(), payment.getCreatedBy());
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
