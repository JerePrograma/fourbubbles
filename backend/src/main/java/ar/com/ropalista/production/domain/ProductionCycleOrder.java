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

import java.time.OffsetDateTime;

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

    @Column(name = "separation_container_code", length = 80)
    private String separationContainerCode;

    @Column(name = "separation_confirmed_at")
    private OffsetDateTime separationConfirmedAt;

    @Column(name = "separation_confirmed_by", length = 100)
    private String separationConfirmedBy;

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

    public boolean isSeparationConfirmed() {
        return !separationRequired || separationConfirmedAt != null;
    }

    public void confirmSeparation(String containerCode, String actor, OffsetDateTime at) {
        if (!separationRequired) {
            throw new IllegalStateException("La asignación no requiere separación física");
        }
        if (separationConfirmedAt != null) {
            if (separationContainerCode.equalsIgnoreCase(containerCode)) {
                return;
            }
            throw new IllegalStateException("La separación física ya fue confirmada con otro contenedor");
        }
        separationContainerCode = containerCode;
        separationConfirmedAt = at;
        separationConfirmedBy = actor;
    }
}
