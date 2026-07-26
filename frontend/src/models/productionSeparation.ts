export interface ProductionSeparation {
  cycleId: string;
  cycleNumber: string;
  orderId: string;
  orderNumber: string;
  separationRequired: boolean;
  containerCode: string | null;
  confirmedAt: string | null;
  confirmedBy: string | null;
}
