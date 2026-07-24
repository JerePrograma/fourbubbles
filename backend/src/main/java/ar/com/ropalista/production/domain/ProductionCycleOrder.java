package ar.com.ropalista.production.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.order.domain.LaundryOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "production_cycle_orders")
public class ProductionCycleOrder extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ProductionCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private LaundryOrder order;

    @Column(name = "assignment_order", nullable = false)
    private int assignmentOrder;

    @Column(name = "assigned_weight_grams", nullable = false)
    private int assignedWeightGrams;

    @Column(name = "separation_required", nullable = false)
    private boolean separationRequired;

    public ProductionCycleOrder(LaundryOrder order, int assignmentOrder,
                                int assignedWeightGrams, boolean separationRequired) {
        this.order = order;
        this.assignmentOrder = assignmentOrder;
        this.assignedWeightGrams = assignedWeightGrams;
        this.separationRequired = separationRequired;
    }

    void attach(ProductionCycle cycle) {
        this.cycle = cycle;
    }
}
