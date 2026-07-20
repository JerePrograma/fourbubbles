package com.fourbubbles.ropalista.pricing.domain;

import com.fourbubbles.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "promotion")
public class Promotion extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 140)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "starts_on", nullable = false)
    private LocalDate startsOn;

    @Column(name = "ends_on")
    private LocalDate endsOn;

    @Column(name = "total_quota")
    private Integer totalQuota;

    @Column(name = "daily_quota")
    private Integer dailyQuota;

    @Column(name = "monthly_quota")
    private Integer monthlyQuota;

    @Column(name = "new_customers_only", nullable = false)
    private boolean newCustomersOnly;

    @Column(name = "one_per_address", nullable = false)
    private boolean onePerAddress;

    @Column(nullable = false)
    private boolean stackable;

    @Column(name = "fixed_price", precision = 19, scale = 2)
    private BigDecimal fixedPrice;

    @Column(name = "percentage_discount", precision = 7, scale = 4)
    private BigDecimal percentageDiscount;

    @Column(name = "credit_amount", precision = 19, scale = 2)
    private BigDecimal creditAmount;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "cancellation_reason", length = 300)
    private String cancellationReason;

    public boolean isValidOn(LocalDate date) {
        return active && !startsOn.isAfter(date) && (endsOn == null || !endsOn.isBefore(date)) && !isDeleted();
    }
}
