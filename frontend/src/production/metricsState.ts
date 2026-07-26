import type { ProductionMetrics } from '../models/productionMetrics';

export function completionLabel(metrics: ProductionMetrics): string {
  return `${metrics.completedCycles} de ${metrics.totalCycles}`;
}

export function separationLabel(metrics: ProductionMetrics): string {
  if (metrics.separationRequiredCycles === 0) return 'Sin ciclos exceptuados';
  return metrics.separationPendingCycles === 0
    ? 'Todos listos'
    : `${metrics.separationPendingCycles} pendientes`;
}
