import type { GarmentEquivalence } from '../models/catalog';

export interface DraftOrderItem {
  equivalenceCode: string;
  physicalPieces: number;
  observations: string;
}

export interface DraftOrderTotals {
  physicalPieces: number;
  equivalentUnits: number;
  estimatedWeightGrams: number | null;
  requiresQuote: boolean;
  exclusiveCycleRequired: boolean;
}

export function calculateOrderDraftTotals(
  items: DraftOrderItem[],
  equivalences: GarmentEquivalence[],
): DraftOrderTotals {
  let physicalPieces = 0;
  let equivalentUnits = 0;
  let estimatedWeightGrams = 0;
  let weightKnown = true;
  let requiresQuote = false;
  let exclusiveCycleRequired = false;

  for (const item of items) {
    const rule = equivalences.find((candidate) => candidate.code === item.equivalenceCode);
    if (!rule || item.physicalPieces <= 0) continue;
    const groups = Math.ceil(item.physicalPieces / rule.physicalUnitsPerGroup);
    physicalPieces += item.physicalPieces;
    equivalentUnits += groups * rule.equivalentUnits;
    if (rule.estimatedWeightGrams === null) weightKnown = false;
    else estimatedWeightGrams += groups * rule.estimatedWeightGrams;
    requiresQuote ||= rule.quoteRequired;
    exclusiveCycleRequired ||= rule.exclusiveCycleRequired;
  }

  return {
    physicalPieces,
    equivalentUnits,
    estimatedWeightGrams: weightKnown ? estimatedWeightGrams : null,
    requiresQuote,
    exclusiveCycleRequired,
  };
}

export function toOffsetDateTime(value: string): string | null {
  if (!value) return null;
  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed.toISOString();
}
