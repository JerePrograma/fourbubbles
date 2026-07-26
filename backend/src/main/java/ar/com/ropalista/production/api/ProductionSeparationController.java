package ar.com.ropalista.production.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.production.application.ProductionSeparationService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/production/cycles/{cycleId}/separations")
public class ProductionSeparationController {
    private final ProductionSeparationService service;

    public ProductionSeparationController(ProductionSeparationService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<List<ProductionSeparationDtos.SeparationResponse>> list(@PathVariable UUID cycleId) {
        return ApiResponse.ok(service.list(cycleId));
    }

    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ProductionSeparationDtos.SeparationResponse> confirm(
            @PathVariable UUID cycleId,
            @PathVariable UUID orderId,
            @Valid @RequestBody ProductionSeparationDtos.ConfirmSeparationRequest request,
            Authentication authentication) {
        return ApiResponse.ok(service.confirm(cycleId, orderId, request, authentication.getName()));
    }
}
