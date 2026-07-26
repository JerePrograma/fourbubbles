package ar.com.ropalista.production.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class ProductionMetricsDtos {
    private ProductionMetricsDtos() {}

    public record MetricsResponse(
            OffsetDateTime from,
            OffsetDateTime to,
            long totalCycles,
            long plannedCycles,
            long runningCycles,
            long completedCycles,
            long cancelledCycles,
            long completedWashCycles,
            long completedDryCycles,
            long sharedCycles,
            long separationRequiredCycles,
            long separationPendingCycles,
            long assignedOrders,
            long plannedWeightGrams,
            long actualWeightGrams,
            BigDecimal averageDurationMinutes,
            BigDecimal completionRatePercent,
            BigDecimal separationReadyPercent) {}
}
