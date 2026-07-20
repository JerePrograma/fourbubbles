package ar.com.ropalista.payment.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.payment.application.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
