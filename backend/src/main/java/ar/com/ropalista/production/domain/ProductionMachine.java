package ar.com.ropalista.production.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "production_machines")
public class ProductionMachine extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "machine_type", nullable = false, length = 20)
    private MachineType machineType;

    @Column(name = "capacity_grams", nullable = false)
    private int capacityGrams;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MachineStatus status;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 1000)
    private String notes;

    public ProductionMachine(String code, String name, MachineType machineType,
                             int capacityGrams, String notes) {
        this.code = code;
        this.name = name;
        this.machineType = machineType;
        this.capacityGrams = capacityGrams;
        this.status = MachineStatus.ACTIVE;
        this.active = true;
        this.notes = notes;
    }

    public void update(String name, int capacityGrams, MachineStatus status,
                       boolean active, String notes) {
        this.name = name;
        this.capacityGrams = capacityGrams;
        this.status = status;
        this.active = active;
        this.notes = notes;
    }

    public boolean isAvailable() {
        return active && status == MachineStatus.ACTIVE;
    }
}
