package ar.com.ropalista.reception.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.reception.application.ReceptionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders/{orderId}/reception")
public class ReceptionController {
    private final ReceptionService service;

    public ReceptionController(ReceptionService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ReceptionDtos.ReceptionResponse> receive(
            @PathVariable UUID orderId,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ReceptionDtos.CreateReceptionRequest request) {
        return ApiResponse.ok(service.receive(orderId, idempotencyKey, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<ReceptionDtos.ReceptionResponse> get(@PathVariable UUID orderId) {
        return ApiResponse.ok(service.get(orderId));
    }

    @PostMapping("/decision")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ReceptionDtos.ReceptionResponse> decide(
            @PathVariable UUID orderId,
            @Valid @RequestBody ReceptionDtos.DecisionRequest request,
            Authentication authentication) {
        return ApiResponse.ok(service.decide(orderId, request, authentication.getName()));
    }
}
