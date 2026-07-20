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
