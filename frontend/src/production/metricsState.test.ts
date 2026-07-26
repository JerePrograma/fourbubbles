import { describe, expect, it } from 'vitest';
import type { ProductionMetrics } from '../models/productionMetrics';
import { completionLabel, separationLabel } from './metricsState';

const metrics: ProductionMetrics = {
  from: '2026-07-01T00:00:00Z',
  to: '2026-08-01T00:00:00Z',
  totalCycles: 5,
  plannedCycles: 1,
  runningCycles: 0,
  completedCycles: 4,
  cancelledCycles: 0,
  completedWashCycles: 3,
  completedDryCycles: 1,
  sharedCycles: 2,
  separationRequiredCycles: 1,
  separationPendingCycles: 1,
  assignedOrders: 7,
  plannedWeightGrams: 12000,
  actualWeightGrams: 11000,
  averageDurationMinutes: 48.5,
  completionRatePercent: 80,
  separationReadyPercent: 0,
};

describe('production metrics state', () => {
  it('formats completion ratio', () => {
    expect(completionLabel(metrics)).toBe('4 de 5');
  });

  it('describes pending and empty separation states', () => {
    expect(separationLabel(metrics)).toBe('1 pendientes');
    expect(separationLabel({ ...metrics, separationRequiredCycles: 0, separationPendingCycles: 0 }))
      .toBe('Sin ciclos exceptuados');
  });
});
