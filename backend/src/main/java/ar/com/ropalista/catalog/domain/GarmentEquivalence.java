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
@Table(name = "garment_equivalences")
public class GarmentEquivalence extends AuditableEntity {
    @Column(nullable = false, length = 60)
    private String code;
    @Column(nullable = false, length = 140)
    private String name;
    @Column(nullable = false, length = 100)
    private String category;
    @Column(name = "physical_units_per_group", nullable = false)
    private int physicalUnitsPerGroup;
    @Column(name = "equivalent_units", nullable = false, precision = 10, scale = 2)
    private BigDecimal equivalentUnits;
    @Column(name = "estimated_weight_grams")
    private Integer estimatedWeightGrams;
    @Column(name = "estimated_volume_units", precision = 10, scale = 2)
    private BigDecimal estimatedVolumeUnits;
    @Column(name = "common_wash_allowed", nullable = false)
    private boolean commonWashAllowed;
    @Column(name = "dryer_allowed", nullable = false)
    private boolean dryerAllowed;
    @Column(name = "exclusive_cycle_required", nullable = false)
    private boolean exclusiveCycleRequired;
    @Column(name = "quote_required", nullable = false)
    private boolean quoteRequired;
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;
    @Column(name = "valid_to")
    private LocalDate validTo;
    @Column(nullable = false)
    private boolean active = true;
}
