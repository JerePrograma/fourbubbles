package ar.com.ropalista.pricing.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "promotions")
public class Promotion extends AuditableEntity {
    @Column(nullable = false, length = 60)
    private String code;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(length = 1200)
    private String description;
    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;
    @Column(name = "valid_to")
    private OffsetDateTime validTo;
    @Column(name = "total_quota")
    private Integer totalQuota;
    @Column(name = "daily_quota")
    private Integer dailyQuota;
    @Column(name = "monthly_quota")
    private Integer monthlyQuota;
    @Column(name = "new_customers_only", nullable = false)
    private boolean newCustomersOnly;
    @Column(name = "automatic_applicable", nullable = false)
    private boolean automaticApplicable;
    @Column(name = "applicable_service_code", length = 50)
    private String applicableServiceCode;
    @Column(name = "one_per_address", nullable = false)
    private boolean onePerAddress;
    @Column(nullable = false)
    private boolean stackable;
    @Column(name = "fixed_price", precision = 15, scale = 2)
    private BigDecimal fixedPrice;
    @Column(name = "percentage_discount", precision = 7, scale = 4)
    private BigDecimal percentageDiscount;
    @Column(name = "credit_amount", precision = 15, scale = 2)
    private BigDecimal creditAmount;
    @Column(nullable = false, length = 30)
    private String status;
}
