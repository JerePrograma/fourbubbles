package com.fourbubbles.ropalista.order.domain;

import com.fourbubbles.ropalista.common.domain.AuditableEntity;
import com.fourbubbles.ropalista.customer.domain.Address;
import com.fourbubbles.ropalista.customer.domain.Customer;
import com.fourbubbles.ropalista.pricing.domain.PriceVersion;
import com.fourbubbles.ropalista.pricing.domain.Promotion;
import com.fourbubbles.ropalista.pricing.domain.ServicePlan;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "laundry_order")
public class LaundryOrder extends AuditableEntity {
    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_plan_id", nullable = false)
    private ServicePlan servicePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_version_id")
    private PriceVersion priceVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus status;

    @Column(nullable = false, length = 30)
    private String modality;

    @Column(name = "exclusive_cycle", nullable = false)
    private boolean exclusiveCycle;

    @Column(name = "physical_piece_count", nullable = false)
    private int physicalPieceCount;

    @Column(name = "equivalent_units", nullable = false, precision = 10, scale = 2)
    private BigDecimal equivalentUnits = BigDecimal.ZERO;

    @Column(name = "declared_weight_grams")
    private Integer declaredWeightGrams;

    @Column(name = "actual_weight_grams")
    private Integer actualWeightGrams;

    @Column(name = "quoted_base_amount", precision = 19, scale = 2)
    private BigDecimal quotedBaseAmount;

    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "credit_amount", precision = 19, scale = 2)
    private BigDecimal creditAmount;

    @Column(name = "quoted_amount", precision = 19, scale = 2)
    private BigDecimal quotedAmount;

    @Column(name = "confirmed_amount", precision = 19, scale = 2)
    private BigDecimal confirmedAmount;

    @Column(nullable = false, length = 3)
    private String currency = "ARS";

    @Column(name = "pricing_explanation", columnDefinition = "text")
    private String pricingExplanation;

    @Column(name = "pickup_scheduled_at")
    private Instant pickupScheduledAt;

    @Column(name = "promised_at")
    private Instant promisedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(columnDefinition = "text")
    private String notes;
}
