package com.fourbubbles.ropalista.audit.application;

import com.fourbubbles.ropalista.audit.domain.AuditLog;
import com.fourbubbles.ropalista.audit.infrastructure.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {
    private final AuditLogRepository logs;

    public AuditService(AuditLogRepository logs) {
        this.logs = logs;
    }

    public void record(String entityType, UUID entityId, String action,
                       String oldValue, String newValue, String reason) {
        AuditLog log = new AuditLog();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setReason(reason);
        logs.save(log);
    }
}
