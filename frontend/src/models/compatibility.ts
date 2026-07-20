export type ColorGroup = 'WHITES' | 'LIGHT' | 'DARK' | 'MIXED' | 'UNKNOWN';
export type MaterialGroup = 'COTTON' | 'SYNTHETIC' | 'DELICATE' | 'WOOL' | 'MIXED';
export type FragrancePolicy = 'NONE' | 'STANDARD' | 'CUSTOM';

export interface TreatmentProfile {
  id: string;
  orderId: string;
  receptionId: string;
  version: number;
  colorGroup: ColorGroup;
  materialGroup: MaterialGroup;
  maxTemperatureC: number;
  dryerAllowed: boolean;
  fragrancePolicy: FragrancePolicy;
  softenerAllowed: boolean;
  hypoallergenic: boolean;
  babyClothes: boolean;
  petContact: boolean;
  heavySoil: boolean;
  exclusiveCycle: boolean;
  notes: string | null;
}

export interface CompatibilityReason {
  code: string;
  severity: 'HARD' | 'WARNING';
  message: string;
}

export interface CompatibilityRecommendation {
  maxTemperatureC: number;
  dryerAllowed: boolean;
  softenerAllowed: boolean;
  fragrancePolicy: FragrancePolicy;
  programMode: 'NORMAL' | 'GENTLE';
  cycleMode: 'SHARED' | 'BLOCKED';
}

export interface CompatibilityException {
  id: string;
  reason: string;
  authorizedBy: string;
  authorizedAt: string;
}

export interface CompatibilityEvaluation {
  id: string;
  orderAId: string;
  orderBId: string;
  profileAVersion: number;
  profileBVersion: number;
  ruleVersion: string;
  compatible: boolean;
  overridden: boolean;
  effectivelyCompatible: boolean;
  reasons: CompatibilityReason[];
  recommendation: CompatibilityRecommendation;
  exception: CompatibilityException | null;
}
