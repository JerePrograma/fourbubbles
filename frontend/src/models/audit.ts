export interface AuditEvent {
  id: string;
  entityType: string;
  entityId: string;
  action: string;
  oldValue: string | null;
  newValue: string | null;
  reason: string | null;
  createdAt: string;
  createdBy: string;
}
