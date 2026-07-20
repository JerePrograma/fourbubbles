package ar.com.ropalista.audit.persistence;

import ar.com.ropalista.audit.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {}
