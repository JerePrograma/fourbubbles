package ar.com.ropalista.audit.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class AuditDtos {
    private AuditDtos() {}

    public record AuditEventResponse(UUID id, String entityType, String entityId, String action,
                                     String oldValue, String newValue, String reason,
                                     OffsetDateTime createdAt, String createdBy) {}
}
