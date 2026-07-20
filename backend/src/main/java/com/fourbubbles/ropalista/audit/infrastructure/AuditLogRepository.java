package com.fourbubbles.ropalista.audit.infrastructure;

import com.fourbubbles.ropalista.audit.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}
