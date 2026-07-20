package ar.com.ropalista.payment.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.payment.application.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<PaymentDtos.PaymentResponse> register(@Valid @RequestBody PaymentDtos.RegisterPaymentRequest request) {
        return ApiResponse.ok(service.register(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','REPORT_VIEWER')")
    ApiResponse<List<PaymentDtos.PaymentHistoryResponse>> history(@RequestParam UUID orderId) {
        return ApiResponse.ok(service.history(orderId));
    }
}
