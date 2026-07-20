package ar.com.ropalista.customer.api;

import ar.com.ropalista.common.api.ApiResponse;
import ar.com.ropalista.customer.application.ClientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {
    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ClientDtos.ClientResponse> create(@Valid @RequestBody ClientDtos.CreateClientRequest request) {
        return ApiResponse.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ClientDtos.ClientResponse> update(@PathVariable UUID id,
                                                   @Valid @RequestBody ClientDtos.UpdateClientRequest request) {
        return ApiResponse.ok(service.update(id, request));
    }

    @PostMapping("/{id}/addresses")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ClientDtos.ClientResponse> addAddress(@PathVariable UUID id,
                                                       @Valid @RequestBody ClientDtos.AddressRequest request) {
        return ApiResponse.ok(service.addAddress(id, request));
    }

    @PostMapping("/{id}/addresses/{addressId}/make-primary")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ClientDtos.ClientResponse> makePrimary(@PathVariable UUID id, @PathVariable UUID addressId) {
        return ApiResponse.ok(service.makePrimary(id, addressId));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    ApiResponse<ClientDtos.ClientResponse> deactivateAddress(@PathVariable UUID id, @PathVariable UUID addressId) {
        return ApiResponse.ok(service.deactivateAddress(id, addressId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','REPORT_VIEWER')")
    ApiResponse<ClientDtos.ClientResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(service.get(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR','REPORT_VIEWER')")
    ApiResponse<Page<ClientDtos.ClientResponse>> search(
            @RequestParam(defaultValue = "") String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(service.search(lastName, page, size));
    }
}
