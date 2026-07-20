export type OrderStatus =
  | 'INQUIRY'
  | 'QUOTED'
  | 'WAITING_CONFIRMATION'
  | 'RESERVED'
  | 'PICKUP_SCHEDULED'
  | 'PICKED_UP'
  | 'RECEIVED'
  | 'PENDING_INSPECTION'
  | 'WAITING_PRICE_APPROVAL'
  | 'CLASSIFIED'
  | 'WAITING_WASH'
  | 'WASHING'
  | 'DRYING'
  | 'QUALITY_CONTROL'
  | 'REWASH_REQUIRED'
  | 'FOLDING'
  | 'PACKAGED'
  | 'READY_FOR_DELIVERY'
  | 'PAYMENT_PENDING'
  | 'DELIVERY_SCHEDULED'
  | 'DELIVERED'
  | 'CLOSED'
  | 'CANCELLED'
  | 'CLAIM'
  | 'PARTIALLY_REFUNDED'
  | 'FULLY_REFUNDED';

export interface OrderSummary {
  id: string;
  orderNumber: string;
  clientId: string;
  clientName: string;
  serviceCode: string;
  serviceName: string;
  status: OrderStatus;
  paymentStatus: string;
  physicalPieces: number;
  equivalentUnits: number;
  quotedPrice: number;
  confirmedPrice: number | null;
  currencyCode: string;
  pickupScheduledAt: string | null;
  promisedAt: string | null;
  createdAt: string;
}

export interface OrderItem {
  equivalenceCode: string;
  name: string;
  physicalPieces: number;
  groups: number;
  equivalentUnits: number;
  estimatedWeightGrams: number | null;
  observations: string | null;
}

export interface OrderDetail {
  id: string;
  orderNumber: string;
  clientId: string;
  addressId: string;
  serviceCode: string;
  status: OrderStatus;
  paymentStatus: string;
  physicalPieces: number;
  equivalentUnits: number;
  declaredWeightGrams: number | null;
  actualWeightGrams: number | null;
  exclusiveCycle: boolean;
  requiresQuote: boolean;
  limitReached: string;
  automaticQuotedPrice: number;
  quotedPrice: number;
  confirmedPrice: number | null;
  currencyCode: string;
  priceBreakdown: string;
  manualQuoteReason: string | null;
  manualQuoteAt: string | null;
  manualQuoteBy: string | null;
  pickupScheduledAt: string | null;
  promisedAt: string | null;
  notes: string | null;
  allowedTransitions: OrderStatus[];
  items: OrderItem[];
}

export interface CreatedOrder {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  quotedPrice: number;
  confirmedPrice: number | null;
  currencyCode: string;
  physicalPieces: number;
  equivalentUnits: number;
  requiresQuote: boolean;
  limitReached: string;
}

export interface PaymentResult {
  id: string;
  orderId: string;
  methodCode: string;
  amount: number;
  currencyCode: string;
  paidAt: string;
  reference: string | null;
  totalPaid: number;
  remainingBalance: number;
  orderPaymentStatus: string;
}

export interface PaymentHistoryItem {
  id: string;
  orderId: string;
  methodCode: string;
  methodName: string;
  amount: number;
  currencyCode: string;
  paidAt: string;
  reference: string | null;
  notes: string | null;
  status: string;
  registeredBy: string;
}
