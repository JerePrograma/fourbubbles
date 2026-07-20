package ar.com.ropalista.order.domain;

import ar.com.ropalista.catalog.domain.GarmentEquivalence;
import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private LaundryOrder order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equivalence_id", nullable = false)
    private GarmentEquivalence equivalence;
    @Column(name = "physical_pieces", nullable = false)
    private int physicalPieces;
    @Column(name = "group_count", nullable = false)
    private int groupCount;
    @Column(name = "equivalent_units_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal equivalentUnitsApplied;
    @Column(name = "estimated_weight_grams")
    private Integer estimatedWeightGrams;
    @Column(length = 500)
    private String observations;

    public OrderItem(GarmentEquivalence equivalence, int physicalPieces, int groupCount,
                     BigDecimal equivalentUnitsApplied, Integer estimatedWeightGrams, String observations) {
        this.equivalence = equivalence;
        this.physicalPieces = physicalPieces;
        this.groupCount = groupCount;
        this.equivalentUnitsApplied = equivalentUnitsApplied;
        this.estimatedWeightGrams = estimatedWeightGrams;
        this.observations = observations;
    }

    void attach(LaundryOrder order) {
        this.order = order;
    }
}
