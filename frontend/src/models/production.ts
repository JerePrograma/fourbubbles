export type MachineType = 'WASHER' | 'DRYER';
export type MachineStatus = 'ACTIVE' | 'MAINTENANCE' | 'OUT_OF_SERVICE';
export type ProductionStage = 'WASH' | 'DRY';
export type ProductionCycleStatus = 'PLANNED' | 'RUNNING' | 'COMPLETED' | 'CANCELLED';
export type QualityDecision = 'PASS' | 'REWASH';
export type FragrancePolicy = 'NONE' | 'STANDARD' | 'CUSTOM';

export interface ProductionMachine {
  id: string;
  code: string;
  name: string;
  machineType: MachineType;
  capacityGrams: number;
  status: MachineStatus;
  active: boolean;
  notes: string | null;
  version: number;
}

export interface ProductionProgram {
  id: string;
  code: string;
  name: string;
  stage: ProductionStage;
  requiredMachineType: MachineType;
  durationMinutes: number;
  maxTemperatureC: number | null;
  gentle: boolean;
  usesSoftener: boolean;
  fragrancePolicy: FragrancePolicy | null;
  active: boolean;
  notes: string | null;
  version: number;
}

export interface ProductionCycleOrder {
  orderId: string;
  orderNumber: string;
  assignmentOrder: number;
  assignedWeightGrams: number;
  separationRequired: boolean;
  orderStatus: string;
}

export interface ProductionCycleHistory {
  previousStatus: string | null;
  newStatus: string;
  observation: string | null;
  occurredAt: string;
  actor: string;
}

export interface ProductionCycle {
  id: string;
  cycleNumber: string;
  status: ProductionCycleStatus;
  machineId: string;
  machineCode: string;
  machineType: MachineType;
  programId: string;
  programCode: string;
  stage: ProductionStage;
  plannedWeightGrams: number;
  actualWeightGrams: number | null;
  notes: string | null;
  startedAt: string | null;
  completedAt: string | null;
  cancelledAt: string | null;
  createdAt: string;
  orders: ProductionCycleOrder[];
  history: ProductionCycleHistory[];
}
