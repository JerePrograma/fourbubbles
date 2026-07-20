export interface ServicePlan {
  id: string;
  code: string;
  name: string;
  description?: string;
  maxEquivalentUnits?: number;
  maxWeightGrams?: number;
}

export interface OrderItem {
  id: string;
  garmentEquivalenceId: string;
  name: string;
  physicalPieceCount: number;
  groupCount: number;
  equivalentUnits: number;
  observations?: string;
}

export interface LaundryOrder {
  id: string;
  orderNumber: string;
  customerId: string;
  customerName: string;
  addressId: string;
  servicePlanId: string;
  serviceName: string;
  status: string;
  modality: string;
  exclusiveCycle: boolean;
  physicalPieceCount: number;
  equivalentUnits: number;
  declaredWeightGrams?: number;
  actualWeightGrams?: number;
  quotedAmount?: number;
  confirmedAmount?: number;
  currency: string;
  pricingExplanation?: string;
  pickupScheduledAt?: string;
  promisedAt?: string;
  deliveredAt?: string;
  items: OrderItem[];
}
