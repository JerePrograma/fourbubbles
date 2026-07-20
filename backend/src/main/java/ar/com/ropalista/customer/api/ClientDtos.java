package ar.com.ropalista.customer.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public final class ClientDtos {
    private ClientDtos() {}

    public record CreateClientRequest(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Size(max = 30) String phone,
            @NotBlank @Size(max = 30) String whatsapp,
            @Email @Size(max = 160) String email,
            @Size(max = 100) String acquisitionSource,
            String preferencesJson,
            @Size(max = 2000) String notes,
            @NotEmpty List<@Valid AddressRequest> addresses
    ) {}

    public record AddressRequest(
            @NotBlank String zoneCode,
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 20) String number,
            @Size(max = 120) String extra,
            @NotBlank @Size(max = 120) String locality,
            @Size(max = 120) String neighborhood,
            @Size(max = 500) String references,
            boolean primaryAddress
    ) {}

    public record ClientResponse(UUID id, String firstName, String lastName, String phone,
                                 String whatsapp, String email, String status, String acquisitionSource,
                                 String preferencesJson, String notes, List<AddressResponse> addresses) {}

    public record AddressResponse(UUID id, String zoneCode, String zoneName, String street, String number,
                                  String extra, String locality, String neighborhood, String references,
                                  boolean primaryAddress) {}
}
