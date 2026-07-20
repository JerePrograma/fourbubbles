export interface ServiceOffering {
  code: string;
  name: string;
  description: string | null;
  maxEquivalentUnits: number | null;
  maxWeightGrams: number | null;
  safeCapacityGrams: number | null;
  requiresQuote: boolean;
}

export interface GarmentEquivalence {
  code: string;
  name: string;
  category: string;
  physicalUnitsPerGroup: number;
  equivalentUnits: number;
  estimatedWeightGrams: number | null;
  dryerAllowed: boolean;
  exclusiveCycleRequired: boolean;
  quoteRequired: boolean;
}
