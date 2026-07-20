package com.fourbubbles.ropalista.garment.domain;

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
@Table(name = "garment_equivalence")
public class GarmentEquivalence extends AuditableEntity {
    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(name = "physical_units_per_group", nullable = false)
    private int physicalUnitsPerGroup;

    @Column(name = "equivalent_units", nullable = false, precision = 10, scale = 2)
    private BigDecimal equivalentUnits;

    @Column(name = "estimated_weight_grams")
    private Integer estimatedWeightGrams;

    @Column(name = "estimated_volume", precision = 10, scale = 2)
    private BigDecimal estimatedVolume;

    @Column(name = "common_wash_allowed", nullable = false)
    private boolean commonWashAllowed;

    @Column(name = "dryer_allowed", nullable = false)
    private boolean dryerAllowed;

    @Column(name = "exclusive_cycle_required", nullable = false)
    private boolean exclusiveCycleRequired;

    @Column(name = "quote_required", nullable = false)
    private boolean quoteRequired;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    public boolean isValidOn(LocalDate date) {
        return active && !validFrom.isAfter(date) && (validTo == null || !validTo.isBefore(date)) && !isDeleted();
    }
}
