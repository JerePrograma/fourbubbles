package ar.com.ropalista.production.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
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

@Getter
@NoArgsConstructor
@Entity
@Table(name = "production_cycle_history")
public class ProductionCycleHistory extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private ProductionCycle cycle;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20)
    private ProductionCycleStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 20)
    private ProductionCycleStatus newStatus;

    @Column(length = 1000)
    private String observation;

    public ProductionCycleHistory(ProductionCycle cycle, ProductionCycleStatus previousStatus,
                                  ProductionCycleStatus newStatus, String observation) {
        this.cycle = cycle;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.observation = observation;
    }
}
