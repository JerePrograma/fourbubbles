package com.fourbubbles.ropalista.customer.api;

import com.fourbubbles.ropalista.common.api.ApiResponse;
import com.fourbubbles.ropalista.customer.application.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@PreAuthorize("hasAnyRole('ADMIN','OPERATOR','REPORT_VIEWER')")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    ApiResponse<List<CustomerDtos.CustomerSummary>> list() {
        return ApiResponse.of(service.list());
    }

    @GetMapping("/{id}")
    ApiResponse<CustomerDtos.CustomerResponse> get(@PathVariable UUID id) {
        return ApiResponse.of(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<CustomerDtos.CustomerResponse> create(
            @Valid @RequestBody CustomerDtos.CreateCustomerRequest request) {
        return ApiResponse.of(service.create(request));
    }
}
