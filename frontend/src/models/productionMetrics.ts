export interface ProductionMetrics {
  from: string;
  to: string;
  totalCycles: number;
  plannedCycles: number;
  runningCycles: number;
  completedCycles: number;
  cancelledCycles: number;
  completedWashCycles: number;
  completedDryCycles: number;
  sharedCycles: number;
  separationRequiredCycles: number;
  separationPendingCycles: number;
  assignedOrders: number;
  plannedWeightGrams: number;
  actualWeightGrams: number;
  averageDurationMinutes: number;
  completionRatePercent: number;
  separationReadyPercent: number;
}
