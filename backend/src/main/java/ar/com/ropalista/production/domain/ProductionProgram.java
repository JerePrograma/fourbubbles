package ar.com.ropalista.production.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.compatibility.domain.FragrancePolicy;
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
@Table(name = "production_programs")
public class ProductionProgram extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 140)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductionStage stage;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_machine_type", nullable = false, length = 20)
    private MachineType requiredMachineType;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "max_temperature_c")
    private Integer maxTemperatureC;

    @Column(nullable = false)
    private boolean gentle;

    @Column(name = "uses_softener", nullable = false)
    private boolean usesSoftener;

    @Enumerated(EnumType.STRING)
    @Column(name = "fragrance_policy", length = 30)
    private FragrancePolicy fragrancePolicy;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 1000)
    private String notes;

    public ProductionProgram(String code, String name, ProductionStage stage,
                             MachineType requiredMachineType, int durationMinutes,
                             Integer maxTemperatureC, boolean gentle, boolean usesSoftener,
                             FragrancePolicy fragrancePolicy, String notes) {
        this.code = code;
        this.name = name;
        this.stage = stage;
        this.requiredMachineType = requiredMachineType;
        this.durationMinutes = durationMinutes;
        this.maxTemperatureC = maxTemperatureC;
        this.gentle = gentle;
        this.usesSoftener = usesSoftener;
        this.fragrancePolicy = fragrancePolicy;
        this.active = true;
        this.notes = notes;
    }

    public void update(String name, int durationMinutes, Integer maxTemperatureC,
                       boolean gentle, boolean usesSoftener,
                       FragrancePolicy fragrancePolicy, boolean active, String notes) {
        this.name = name;
        this.durationMinutes = durationMinutes;
        this.maxTemperatureC = maxTemperatureC;
        this.gentle = gentle;
        this.usesSoftener = usesSoftener;
        this.fragrancePolicy = fragrancePolicy;
        this.active = active;
        this.notes = notes;
    }
}
