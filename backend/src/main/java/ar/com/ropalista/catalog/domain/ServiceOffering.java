package ar.com.ropalista.catalog.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "service_offerings")
public class ServiceOffering extends AuditableEntity {
    @Column(nullable = false, length = 50)
    private String code;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(length = 1000)
    private String description;
    @Column(name = "max_equivalent_units", precision = 10, scale = 2)
    private BigDecimal maxEquivalentUnits;
    @Column(name = "max_weight_grams")
    private Integer maxWeightGrams;
    @Column(name = "safe_capacity_grams")
    private Integer safeCapacityGrams;
    @Column(name = "requires_quote", nullable = false)
    private boolean requiresQuote;
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    @Column(name = "valid_to")
    private LocalDate validTo;
    @Column(nullable = false)
    private boolean active = true;
}
