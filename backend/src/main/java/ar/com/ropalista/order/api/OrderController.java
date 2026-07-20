package ar.com.ropalista.order.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.order.application.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<OrderDtos.OrderResponse> create(@Valid @RequestBody OrderDtos.CreateOrderRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER')")
    ApiResponse<OrderDtos.OrderResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @PostMapping("/{id}/confirm-price")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<OrderDtos.OrderResponse> confirmPrice(@PathVariable UUID id) {
        return ApiResponse.ok(service.confirmPrice(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','DRIVER')")
    ApiResponse<OrderDtos.OrderResponse> changeStatus(@PathVariable UUID id,
                                                       @Valid @RequestBody OrderDtos.ChangeStatusRequest request) {
        return ApiResponse.ok(service.changeStatus(id, request));
    }
}
