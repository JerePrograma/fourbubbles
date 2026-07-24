package ar.com.ropalista.production.api;

import ar.com.ropalista.compatibility.domain.FragrancePolicy;
import ar.com.ropalista.production.domain.MachineStatus;
import ar.com.ropalista.production.domain.MachineType;
import ar.com.ropalista.production.domain.ProductionCycleStatus;
import ar.com.ropalista.production.domain.ProductionStage;
import ar.com.ropalista.production.domain.QualityDecision;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class ProductionDtos {
    private ProductionDtos() {}

    public record MachineRequest(
            @NotBlank @Size(max = 40) String code,
            @NotBlank @Size(max = 120) String name,
            @NotNull MachineType machineType,
            @Positive int capacityGrams,
            @NotNull MachineStatus status,
            boolean active,
            @Size(max = 1000) String notes) {}

    public record MachineResponse(UUID id, String code, String name, MachineType machineType,
                                  int capacityGrams, MachineStatus status, boolean active,
                                  String notes, long version) {}

    public record ProgramRequest(
            @NotBlank @Size(max = 50) String code,
            @NotBlank @Size(max = 140) String name,
            @NotNull ProductionStage stage,
            @Positive int durationMinutes,
            @Min(20) @Max(95) Integer maxTemperatureC,
            boolean gentle,
            boolean usesSoftener,
            FragrancePolicy fragrancePolicy,
            boolean active,
            @Size(max = 1000) String notes) {}

    public record ProgramResponse(UUID id, String code, String name, ProductionStage stage,
                                  MachineType requiredMachineType, int durationMinutes,
                                  Integer maxTemperatureC, boolean gentle, boolean usesSoftener,
                                  FragrancePolicy fragrancePolicy, boolean active,
                                  String notes, long version) {}

    public record CreateCycleRequest(
            @NotNull UUID machineId,
            @NotNull UUID programId,
            @NotEmpty @Size(max = 2) List<@NotNull UUID> orderIds,
            @Size(max = 1500) String notes) {}

    public record CompleteCycleRequest(@Positive int actualWeightGrams,
                                       @Size(max = 1000) String observation) {}

    public record CycleActionRequest(@Size(max = 1000) String observation) {}

    public record QualityControlRequest(@NotNull QualityDecision decision,
                                        @NotBlank @Size(max = 1000) String observation) {}

    public record QualityControlResponse(UUID orderId, String orderNumber,
                                         String previousStatus, String newStatus,
                                         QualityDecision decision, String observation) {}

    public record CycleOrderResponse(UUID orderId, String orderNumber, int assignmentOrder,
                                     int assignedWeightGrams, boolean separationRequired,
                                     String orderStatus) {}

    public record CycleHistoryResponse(String previousStatus, String newStatus,
                                       String observation, OffsetDateTime occurredAt,
                                       String actor) {}

    public record CycleResponse(UUID id, String cycleNumber, ProductionCycleStatus status,
                                UUID machineId, String machineCode, MachineType machineType,
                                UUID programId, String programCode, ProductionStage stage,
                                int plannedWeightGrams, Integer actualWeightGrams,
                                String notes, OffsetDateTime startedAt,
                                OffsetDateTime completedAt, OffsetDateTime cancelledAt,
                                OffsetDateTime createdAt, List<CycleOrderResponse> orders,
                                List<CycleHistoryResponse> history) {}
}
