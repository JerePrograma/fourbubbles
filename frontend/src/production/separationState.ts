import type { ProductionSeparation } from '../models/productionSeparation';

export function normalizeContainerCode(value: string): string {
  return value.trim().toUpperCase();
}

export function pendingSeparationCount(values: ProductionSeparation[]): number {
  return values.filter((value) => value.separationRequired && !value.confirmedAt).length;
}
