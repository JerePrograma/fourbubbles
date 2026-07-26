package ar.com.ropalista.production.application;

import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.production.api.ProductionMetricsDtos;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;

@Service
public class ProductionMetricsService {
    private static final Duration DEFAULT_WINDOW = Duration.ofDays(30);
    private static final Duration MAX_WINDOW = Duration.ofDays(366);

    private static final String METRICS_SQL = """
            with assignment_stats as (
                select
                    cycle_id,
                    count(*) as assigned_orders,
                    count(*) > 1 as shared_cycle,
                    bool_or(separation_required) as separation_required,
                    bool_or(separation_required and separation_confirmed_at is null) as separation_pending
                from production_cycle_orders
                group by cycle_id
            )
            select
                count(*) as total_cycles,
                count(*) filter (where c.status = 'PLANNED') as planned_cycles,
                count(*) filter (where c.status = 'RUNNING') as running_cycles,
                count(*) filter (where c.status = 'COMPLETED') as completed_cycles,
                count(*) filter (where c.status = 'CANCELLED') as cancelled_cycles,
                count(*) filter (where c.status = 'COMPLETED' and p.stage = 'WASH') as completed_wash_cycles,
                count(*) filter (where c.status = 'COMPLETED' and p.stage = 'DRY') as completed_dry_cycles,
                count(*) filter (where coalesce(a.shared_cycle, false)) as shared_cycles,
                count(*) filter (where coalesce(a.separation_required, false)) as separation_required_cycles,
                count(*) filter (where coalesce(a.separation_pending, false)) as separation_pending_cycles,
                coalesce(sum(a.assigned_orders), 0) as assigned_orders,
                coalesce(sum(c.planned_weight_grams), 0) as planned_weight_grams,
                coalesce(sum(c.actual_weight_grams) filter (where c.status = 'COMPLETED'), 0) as actual_weight_grams,
                coalesce(avg(extract(epoch from (c.completed_at - c.started_at)) / 60.0)
                    filter (where c.status = 'COMPLETED'), 0) as average_duration_minutes
            from production_cycles c
            join production_programs p on p.id = c.program_id
            left join assignment_stats a on a.cycle_id = c.id
            where c.created_at >= ? and c.created_at < ?
            """;

    private final JdbcTemplate jdbcTemplate;

    public ProductionMetricsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public ProductionMetricsDtos.MetricsResponse get(OffsetDateTime requestedFrom, OffsetDateTime requestedTo) {
        OffsetDateTime to = requestedTo == null ? OffsetDateTime.now() : requestedTo;
        OffsetDateTime from = requestedFrom == null ? to.minus(DEFAULT_WINDOW) : requestedFrom;
        validateRange(from, to);

        Aggregate aggregate = jdbcTemplate.queryForObject(METRICS_SQL, (resultSet, rowNumber) -> new Aggregate(
                resultSet.getLong("total_cycles"),
                resultSet.getLong("planned_cycles"),
                resultSet.getLong("running_cycles"),
                resultSet.getLong("completed_cycles"),
                resultSet.getLong("cancelled_cycles"),
                resultSet.getLong("completed_wash_cycles"),
                resultSet.getLong("completed_dry_cycles"),
                resultSet.getLong("shared_cycles"),
                resultSet.getLong("separation_required_cycles"),
                resultSet.getLong("separation_pending_cycles"),
                resultSet.getLong("assigned_orders"),
                resultSet.getLong("planned_weight_grams"),
                resultSet.getLong("actual_weight_grams"),
                decimal(resultSet.getBigDecimal("average_duration_minutes"))),
                from, to);

        if (aggregate == null) {
            aggregate = Aggregate.empty();
        }
        return new ProductionMetricsDtos.MetricsResponse(
                from,
                to,
                aggregate.totalCycles(),
                aggregate.plannedCycles(),
                aggregate.runningCycles(),
                aggregate.completedCycles(),
                aggregate.cancelledCycles(),
                aggregate.completedWashCycles(),
                aggregate.completedDryCycles(),
                aggregate.sharedCycles(),
                aggregate.separationRequiredCycles(),
                aggregate.separationPendingCycles(),
                aggregate.assignedOrders(),
                aggregate.plannedWeightGrams(),
                aggregate.actualWeightGrams(),
                aggregate.averageDurationMinutes(),
                percentage(aggregate.completedCycles(), aggregate.totalCycles(), false),
                percentage(
                        aggregate.separationRequiredCycles() - aggregate.separationPendingCycles(),
                        aggregate.separationRequiredCycles(),
                        true));
    }

    private void validateRange(OffsetDateTime from, OffsetDateTime to) {
        if (!from.isBefore(to)) {
            throw new BusinessException("INVALID_METRICS_RANGE",
                    "El inicio debe ser anterior al fin", HttpStatus.BAD_REQUEST);
        }
        if (Duration.between(from, to).compareTo(MAX_WINDOW) > 0) {
            throw new BusinessException("METRICS_RANGE_TOO_LARGE",
                    "El rango de métricas no puede superar 366 días", HttpStatus.BAD_REQUEST);
        }
    }

    private static BigDecimal percentage(long numerator, long denominator, boolean emptyIsComplete) {
        if (denominator == 0) {
            return emptyIsComplete ? new BigDecimal("100.00") : BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private record Aggregate(
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
            BigDecimal averageDurationMinutes) {
        private static Aggregate empty() {
            return new Aggregate(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    BigDecimal.ZERO.setScale(2));
        }
    }
}
