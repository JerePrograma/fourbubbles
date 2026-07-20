export type ReceptionApprovalStatus = 'NOT_REQUIRED' | 'PENDING' | 'APPROVED' | 'REJECTED';

export interface ReceptionItem {
  equivalenceCode: string;
  equivalenceName: string;
  declaredPhysicalPieces: number;
  actualPhysicalPieces: number;
  pieceDifference: number;
  damageDetected: boolean;
  stainDetected: boolean;
  observations: string | null;
}

export interface ReceptionEvidence {
  id: string;
  objectKey: string;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  sha256: string;
  caption: string | null;
}

export interface ReceptionDetail {
  id: string;
  orderId: string;
  idempotencyKey: string;
  receivedAt: string;
  declaredPhysicalPieces: number;
  actualPhysicalPieces: number;
  declaredWeightGrams: number | null;
  actualWeightGrams: number;
  pieceDifference: number;
  weightDifferenceGrams: number | null;
  conditionNotes: string | null;
  damageDetected: boolean;
  stainDetected: boolean;
  requiresCustomerApproval: boolean;
  approvalStatus: ReceptionApprovalStatus;
  approvalAt: string | null;
  approvalBy: string | null;
  approvalNotes: string | null;
  labelCode: string;
  bagCode: string | null;
  orderStatus: string;
  items: ReceptionItem[];
  evidences: ReceptionEvidence[];
}
