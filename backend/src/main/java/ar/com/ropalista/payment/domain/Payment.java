package ar.com.ropalista.payment.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.customer.domain.Client;
import ar.com.ropalista.order.domain.LaundryOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private LaundryOrder order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod method;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @Column(name = "paid_at", nullable = false)
    private OffsetDateTime paidAt;
    @Column(length = 160)
    private String reference;
    @Column(length = 500)
    private String notes;
    @Column(nullable = false, length = 30)
    private String status = "PAID";

    public Payment(LaundryOrder order, PaymentMethod method, BigDecimal amount,
                   String currencyCode, OffsetDateTime paidAt, String reference, String notes) {
        this.order = order;
        this.client = order.getClient();
        this.method = method;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.paidAt = paidAt;
        this.reference = reference;
        this.notes = notes;
    }
}
