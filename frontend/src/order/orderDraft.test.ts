import { describe, expect, it } from 'vitest';
import type { GarmentEquivalence } from '../models/catalog';
import { calculateOrderDraftTotals, toOffsetDateTime } from './orderDraft';

const equivalences: GarmentEquivalence[] = [
  {
    code: 'SOCKS_3_PAIRS', name: 'Tres pares de medias', category: 'PEQUENAS',
    physicalUnitsPerGroup: 6, equivalentUnits: 1, estimatedWeightGrams: 180,
    dryerAllowed: true, exclusiveCycleRequired: false, quoteRequired: false,
  },
  {
    code: 'HEAVY_SWEATSHIRT', name: 'Buzo grueso', category: 'ABRIGO',
    physicalUnitsPerGroup: 1, equivalentUnits: 2, estimatedWeightGrams: 850,
    dryerAllowed: true, exclusiveCycleRequired: false, quoteRequired: false,
  },
  {
    code: 'COMFORTER', name: 'Acolchado', category: 'SERVICIO_SEPARADO',
    physicalUnitsPerGroup: 1, equivalentUnits: 1, estimatedWeightGrams: null,
    dryerAllowed: false, exclusiveCycleRequired: true, quoteRequired: true,
  },
];

describe('calculateOrderDraftTotals', () => {
  it('redondea grupos incompletos hacia arriba sin perder piezas físicas', () => {
    const result = calculateOrderDraftTotals([
      { equivalenceCode: 'SOCKS_3_PAIRS', physicalPieces: 7, observations: '' },
    ], equivalences);

    expect(result.physicalPieces).toBe(7);
    expect(result.equivalentUnits).toBe(2);
    expect(result.estimatedWeightGrams).toBe(360);
  });

  it('acumula equivalencias y marca presupuesto o ciclo exclusivo', () => {
    const result = calculateOrderDraftTotals([
      { equivalenceCode: 'HEAVY_SWEATSHIRT', physicalPieces: 2, observations: '' },
      { equivalenceCode: 'COMFORTER', physicalPieces: 1, observations: '' },
    ], equivalences);

    expect(result.physicalPieces).toBe(3);
    expect(result.equivalentUnits).toBe(5);
    expect(result.estimatedWeightGrams).toBeNull();
    expect(result.requiresQuote).toBe(true);
    expect(result.exclusiveCycleRequired).toBe(true);
  });
});

describe('toOffsetDateTime', () => {
  it('devuelve null para valores vacíos o inválidos', () => {
    expect(toOffsetDateTime('')).toBeNull();
    expect(toOffsetDateTime('no-es-fecha')).toBeNull();
  });

  it('convierte una fecha local válida a ISO con zona', () => {
    expect(toOffsetDateTime('2026-07-21T10:30')).toMatch(/^2026-07-21T\d{2}:30:00\.000Z$/);
  });
});
