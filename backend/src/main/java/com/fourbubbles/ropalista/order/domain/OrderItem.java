package com.fourbubbles.ropalista.order.domain;

import com.fourbubbles.ropalista.common.domain.AuditableEntity;
import com.fourbubbles.ropalista.garment.domain.GarmentEquivalence;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_item")
public class OrderItem extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private LaundryOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garment_equivalence_id", nullable = false)
    private GarmentEquivalence garmentEquivalence;

    @Column(name = "rule_name_snapshot", nullable = false, length = 120)
    private String ruleNameSnapshot;

    @Column(name = "physical_units_per_group_snapshot", nullable = false)
    private int physicalUnitsPerGroupSnapshot;

    @Column(name = "equivalent_units_per_group_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal equivalentUnitsPerGroupSnapshot;

    @Column(name = "physical_piece_count", nullable = false)
    private int physicalPieceCount;

    @Column(name = "group_count", nullable = false)
    private int groupCount;

    @Column(name = "equivalent_units", nullable = false, precision = 10, scale = 2)
    private BigDecimal equivalentUnits;

    @Column(columnDefinition = "text")
    private String observations;
}
