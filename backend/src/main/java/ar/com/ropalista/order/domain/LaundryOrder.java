package ar.com.ropalista.order.domain;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.customer.domain.Address;
import ar.com.ropalista.customer.domain.Client;
import ar.com.ropalista.pricing.domain.PriceDefinition;
import ar.com.ropalista.pricing.domain.Promotion;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "laundry_orders")
public class LaundryOrder extends AuditableEntity {
    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "price_definition_id", nullable = false)
    private PriceDefinition priceDefinition;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus status;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    @Column(name = "physical_pieces", nullable = false)
    private int physicalPieces;
    @Column(name = "equivalent_units", nullable = false, precision = 10, scale = 2)
    private BigDecimal equivalentUnits;
    @Column(name = "declared_weight_grams")
    private Integer declaredWeightGrams;
    @Column(name = "actual_weight_grams")
    private Integer actualWeightGrams;
    @Column(name = "exclusive_cycle", nullable = false)
    private boolean exclusiveCycle;
    @Column(name = "requires_quote", nullable = false)
    private boolean requiresQuote;
    @Column(name = "limit_reached", nullable = false, length = 30)
    private String limitReached;
    @Column(name = "automatic_quoted_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal automaticQuotedPrice;
    @Column(name = "quoted_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal quotedPrice;
    @Column(name = "confirmed_price", precision = 15, scale = 2)
    private BigDecimal confirmedPrice;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "price_breakdown", nullable = false, columnDefinition = "jsonb")
    private String priceBreakdown;
    @Column(name = "manual_quote_reason", length = 1000)
    private String manualQuoteReason;
    @Column(name = "manual_quote_at")
    private OffsetDateTime manualQuoteAt;
    @Column(name = "manual_quote_by", length = 100)
    private String manualQuoteBy;
    @Column(name = "pickup_scheduled_at")
    private OffsetDateTime pickupScheduledAt;
    @Column(name = "promised_at")
    private OffsetDateTime promisedAt;
    @Column(length = 2000)
    private String notes;
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public LaundryOrder(String orderNumber, Client client, Address address, ServiceOffering service,
                        PriceDefinition priceDefinition, Promotion promotion, int physicalPieces,
                        BigDecimal equivalentUnits, Integer declaredWeightGrams, boolean exclusiveCycle,
                        boolean requiresQuote, String limitReached, BigDecimal quotedPrice,
                        String currencyCode, String priceBreakdown, OffsetDateTime pickupScheduledAt,
                        OffsetDateTime promisedAt, String notes) {
        this.orderNumber = orderNumber;
        this.client = client;
        this.address = address;
        this.service = service;
        this.priceDefinition = priceDefinition;
        this.promotion = promotion;
        this.status = requiresQuote ? OrderStatus.INQUIRY : OrderStatus.QUOTED;
        this.physicalPieces = physicalPieces;
        this.equivalentUnits = equivalentUnits;
        this.declaredWeightGrams = declaredWeightGrams;
        this.exclusiveCycle = exclusiveCycle;
        this.requiresQuote = requiresQuote;
        this.limitReached = limitReached;
        this.automaticQuotedPrice = quotedPrice;
        this.quotedPrice = quotedPrice;
        this.currencyCode = currencyCode;
        this.priceBreakdown = priceBreakdown;
        this.pickupScheduledAt = pickupScheduledAt;
        this.promisedAt = promisedAt;
        this.notes = notes;
    }

    public void addItem(OrderItem item) {
        item.attach(this);
        items.add(item);
    }

    public void applyManualQuote(BigDecimal amount, String reason, String actor,
                                 OffsetDateTime at, String updatedBreakdown) {
        if (confirmedPrice != null) {
            throw new IllegalStateException("El precio ya fue confirmado");
        }
        this.quotedPrice = amount;
        this.requiresQuote = false;
        this.manualQuoteReason = reason;
        this.manualQuoteBy = actor;
        this.manualQuoteAt = at;
        this.priceBreakdown = updatedBreakdown;
    }

    public void updatePlanning(OffsetDateTime pickupScheduledAt, OffsetDateTime promisedAt, String notes) {
        if (confirmedPrice != null) {
            throw new IllegalStateException("No se puede editar la planificación después de confirmar el precio");
        }
        this.pickupScheduledAt = pickupScheduledAt;
        this.promisedAt = promisedAt;
        this.notes = notes;
    }

    public void confirmPrice() {
        this.confirmedPrice = this.quotedPrice;
    }

    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void updatePaymentStatus(PaymentStatus newStatus) {
        this.paymentStatus = newStatus;
    }
}
