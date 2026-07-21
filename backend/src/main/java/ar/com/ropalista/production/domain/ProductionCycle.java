package ar.com.ropalista.production.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "production_cycles")
public class ProductionCycle extends AuditableEntity {
    @Column(name = "cycle_number", nullable = false, unique = true, length = 24)
    private String cycleNumber;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "machine_id", nullable = false)
    private ProductionMachine machine;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private ProductionProgram program;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionCycleStatus status;

    @Column(name = "planned_weight_grams", nullable = false)
    private int plannedWeightGrams;

    @Column(name = "actual_weight_grams")
    private Integer actualWeightGrams;

    @Column(length = 1500)
    private String notes;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @OneToMany(mappedBy = "cycle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductionCycleOrder> orders = new ArrayList<>();

    public ProductionCycle(String cycleNumber, String idempotencyKey,
                           ProductionMachine machine, ProductionProgram program,
                           int plannedWeightGrams, String notes) {
        this.cycleNumber = cycleNumber;
        this.idempotencyKey = idempotencyKey;
        this.machine = machine;
        this.program = program;
        this.status = ProductionCycleStatus.PLANNED;
        this.plannedWeightGrams = plannedWeightGrams;
        this.notes = notes;
    }

    public void addOrder(ProductionCycleOrder assignment) {
        assignment.attach(this);
        orders.add(assignment);
    }

    public void start(OffsetDateTime at) {
        if (status != ProductionCycleStatus.PLANNED) {
            throw new IllegalStateException("Solo puede iniciarse un ciclo planificado");
        }
        status = ProductionCycleStatus.RUNNING;
        startedAt = at;
    }

    public void complete(int actualWeightGrams, OffsetDateTime at) {
        if (status != ProductionCycleStatus.RUNNING) {
            throw new IllegalStateException("Solo puede completarse un ciclo en ejecución");
        }
        this.actualWeightGrams = actualWeightGrams;
        this.completedAt = at;
        this.status = ProductionCycleStatus.COMPLETED;
    }

    public void cancel(OffsetDateTime at) {
        if (status != ProductionCycleStatus.PLANNED) {
            throw new IllegalStateException("Solo puede cancelarse un ciclo planificado");
        }
        this.cancelledAt = at;
        this.status = ProductionCycleStatus.CANCELLED;
    }
}
