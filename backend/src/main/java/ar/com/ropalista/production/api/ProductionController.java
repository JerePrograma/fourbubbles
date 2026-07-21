package ar.com.ropalista.production.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.production.application.ProductionService;
import ar.com.ropalista.production.domain.ProductionCycleStatus;
import ar.com.ropalista.production.domain.ProductionStage;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/production")
public class ProductionController {
    private final ProductionService service;

    public ProductionController(ProductionService service) {
        this.service = service;
    }

    @PostMapping("/machines")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductionDtos.MachineResponse> createMachine(
            @Valid @RequestBody ProductionDtos.MachineRequest request) {
        return ApiResponse.ok(service.createMachine(request));
    }

    @PutMapping("/machines/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductionDtos.MachineResponse> updateMachine(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionDtos.MachineRequest request) {
        return ApiResponse.ok(service.updateMachine(id, request));
    }

    @GetMapping("/machines")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<List<ProductionDtos.MachineResponse>> listMachines() {
        return ApiResponse.ok(service.listMachines());
    }

    @PostMapping("/programs")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductionDtos.ProgramResponse> createProgram(
            @Valid @RequestBody ProductionDtos.ProgramRequest request) {
        return ApiResponse.ok(service.createProgram(request));
    }

    @PutMapping("/programs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<ProductionDtos.ProgramResponse> updateProgram(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionDtos.ProgramRequest request) {
        return ApiResponse.ok(service.updateProgram(id, request));
    }

    @GetMapping("/programs")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<List<ProductionDtos.ProgramResponse>> listPrograms(
            @RequestParam(required = false) ProductionStage stage) {
        return ApiResponse.ok(service.listPrograms(stage));
    }

    @PostMapping("/cycles")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ProductionDtos.CycleResponse> createCycle(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody ProductionDtos.CreateCycleRequest request) {
        return ApiResponse.ok(service.createCycle(idempotencyKey, request));
    }

    @GetMapping("/cycles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<ProductionDtos.CycleResponse> getCycle(@PathVariable UUID id) {
        return ApiResponse.ok(service.getCycle(id));
    }

    @GetMapping("/cycles")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<Page<ProductionDtos.CycleResponse>> searchCycles(
            @RequestParam(required = false) ProductionCycleStatus status,
            @RequestParam(required = false) ProductionStage stage,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.searchCycles(status, stage, page, size));
    }

    @PostMapping("/cycles/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ProductionDtos.CycleResponse> startCycle(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionDtos.CycleActionRequest request) {
        return ApiResponse.ok(service.startCycle(id, request));
    }

    @PostMapping("/cycles/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ProductionDtos.CycleResponse> completeCycle(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionDtos.CompleteCycleRequest request) {
        return ApiResponse.ok(service.completeCycle(id, request));
    }

    @PostMapping("/cycles/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ProductionDtos.CycleResponse> cancelCycle(
            @PathVariable UUID id,
            @Valid @RequestBody ProductionDtos.CycleActionRequest request) {
        return ApiResponse.ok(service.cancelCycle(id, request));
    }

    @PatchMapping("/orders/{orderId}/quality-control")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ProductionDtos.QualityControlResponse> qualityControl(
            @PathVariable UUID orderId,
            @Valid @RequestBody ProductionDtos.QualityControlRequest request) {
        var result = service.qualityControl(orderId, request);
        return ApiResponse.ok(new ProductionDtos.QualityControlResponse(
                result.orderId(), result.orderNumber(), "QUALITY_CONTROL",
                result.orderStatus(), request.decision(), request.observation()));
    }
}
