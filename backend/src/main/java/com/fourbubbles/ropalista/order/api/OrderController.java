package com.fourbubbles.ropalista.order.api;

import com.fourbubbles.ropalista.common.api.ApiResponse;
import com.fourbubbles.ropalista.order.application.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasAnyRole('ADMIN','OPERATOR','REPORT_VIEWER')")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    ApiResponse<List<OrderDtos.OrderResponse>> list() {
        return ApiResponse.of(service.list());
    }

    @GetMapping("/{id}")
    ApiResponse<OrderDtos.OrderResponse> get(@PathVariable UUID id) {
        return ApiResponse.of(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<OrderDtos.OrderResponse> create(@Valid @RequestBody OrderDtos.CreateOrderRequest request) {
        return ApiResponse.of(service.create(request));
    }

    @PostMapping("/{id}/reception")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<OrderDtos.OrderResponse> receive(@PathVariable UUID id,
            @Valid @RequestBody OrderDtos.ReceiveOrderRequest request) {
        return ApiResponse.of(service.receive(id, request));
    }

    @PostMapping("/{id}/quote")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<OrderDtos.PriceQuoteResponse> quote(@PathVariable UUID id,
            @Valid @RequestBody OrderDtos.QuoteRequest request) {
        return ApiResponse.of(service.quote(id, request));
    }

    @PostMapping("/{id}/confirm-price")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<OrderDtos.OrderResponse> confirmPrice(@PathVariable UUID id) {
        return ApiResponse.of(service.confirmPrice(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER')")
    ApiResponse<OrderDtos.OrderResponse> transition(@PathVariable UUID id,
            @Valid @RequestBody OrderDtos.TransitionRequest request) {
        return ApiResponse.of(service.transition(id, request));
    }
}
