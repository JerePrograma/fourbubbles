package ar.com.ropalista.audit.application;

import ar.com.ropalista.audit.domain.AuditEvent;
import ar.com.ropalista.audit.persistence.AuditEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditEventRepository repository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public void record(String entityType, Object entityId, String action, Object oldValue, Object newValue, String reason) {
        repository.save(new AuditEvent(entityType, String.valueOf(entityId), action,
                json(oldValue), json(newValue), reason));
    }

    private String json(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("No se pudo serializar la auditoría", ex);
        }
    }
}
