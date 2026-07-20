package ar.com.ropalista.audit.api;

import ar.com.ropalista.audit.application.AuditQueryService;
import ar.com.ropalista.common.api.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/audit")
public class AuditController {
    private final AuditQueryService service;

    public AuditController(AuditQueryService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Page<AuditDtos.AuditEventResponse>> search(
            @RequestParam(defaultValue = "") String entityType,
            @RequestParam(defaultValue = "") String entityId,
            @RequestParam(defaultValue = "") String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.search(entityType, entityId, action, page, size));
    }
}
