import { describe, expect, it } from 'vitest';
import type { ProductionSeparation } from '../models/productionSeparation';
import { normalizeContainerCode, pendingSeparationCount } from './separationState';

const base: ProductionSeparation = {
  cycleId: 'cycle-1',
  cycleNumber: 'PC-000001',
  orderId: 'order-1',
  orderNumber: 'RL-000001',
  separationRequired: true,
  containerCode: null,
  confirmedAt: null,
  confirmedBy: null,
};

describe('separation state', () => {
  it('normalizes physical container codes', () => {
    expect(normalizeContainerCode(' bag-a ')).toBe('BAG-A');
  });

  it('counts only pending required separations', () => {
    expect(pendingSeparationCount([
      base,
      { ...base, orderId: 'order-2', confirmedAt: '2026-07-26T12:00:00Z' },
      { ...base, orderId: 'order-3', separationRequired: false },
    ])).toBe(1);
  });
});
