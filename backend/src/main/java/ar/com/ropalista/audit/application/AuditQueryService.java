package ar.com.ropalista.audit.application;

import ar.com.ropalista.audit.api.AuditDtos;
import ar.com.ropalista.audit.domain.AuditEvent;
import ar.com.ropalista.audit.persistence.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditQueryService {
    private final AuditEventRepository events;

    public AuditQueryService(AuditEventRepository events) {
        this.events = events;
    }

    @Transactional(readOnly = true)
    public Page<AuditDtos.AuditEventResponse> search(String entityType, String entityId, String action,
                                                      int page, int size) {
        Specification<AuditEvent> specification = Specification.where(null);
        if (entityType != null && !entityType.isBlank()) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(builder.upper(root.get("entityType")), entityType.trim().toUpperCase()));
        }
        if (entityId != null && !entityId.isBlank()) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("entityId"), entityId.trim()));
        }
        if (action != null && !action.isBlank()) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(builder.upper(root.get("action")), action.trim().toUpperCase()));
        }
        var pageable = PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return events.findAll(specification, pageable).map(this::toResponse);
    }

    private AuditDtos.AuditEventResponse toResponse(AuditEvent event) {
        return new AuditDtos.AuditEventResponse(event.getId(), event.getEntityType(), event.getEntityId(),
                event.getAction(), event.getOldValue(), event.getNewValue(), event.getReason(),
                event.getCreatedAt(), event.getCreatedBy());
    }
}
