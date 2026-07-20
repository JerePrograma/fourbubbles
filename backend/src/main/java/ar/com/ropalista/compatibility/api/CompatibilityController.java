package ar.com.ropalista.compatibility.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.compatibility.application.CompatibilityService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CompatibilityController {
    private final CompatibilityService service;

    public CompatibilityController(CompatibilityService service) {
        this.service = service;
    }

    @PutMapping("/orders/{orderId}/compatibility-profile")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<CompatibilityDtos.TreatmentProfileResponse> saveProfile(
            @PathVariable UUID orderId,
            @Valid @RequestBody CompatibilityDtos.TreatmentProfileRequest request) {
        return ApiResponse.ok(service.saveProfile(orderId, request));
    }

    @GetMapping("/orders/{orderId}/compatibility-profile")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<CompatibilityDtos.TreatmentProfileResponse> getProfile(@PathVariable UUID orderId) {
        return ApiResponse.ok(service.getProfile(orderId));
    }

    @PostMapping("/compatibility/evaluate")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<CompatibilityDtos.EvaluationResponse> evaluate(
            @Valid @RequestBody CompatibilityDtos.EvaluateRequest request) {
        return ApiResponse.ok(service.evaluate(request));
    }

    @GetMapping("/compatibility/evaluations/{evaluationId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<CompatibilityDtos.EvaluationResponse> getEvaluation(@PathVariable UUID evaluationId) {
        return ApiResponse.ok(service.getEvaluation(evaluationId));
    }

    @PostMapping("/compatibility/evaluations/{evaluationId}/exception")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<CompatibilityDtos.EvaluationResponse> authorizeException(
            @PathVariable UUID evaluationId,
            @Valid @RequestBody CompatibilityDtos.ExceptionRequest request,
            Authentication authentication) {
        return ApiResponse.ok(service.authorizeException(evaluationId, request, authentication.getName()));
    }
}
