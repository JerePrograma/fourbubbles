package com.fourbubbles.ropalista.customer.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class CustomerDtos {
    private CustomerDtos() {}

    public record CreateCustomerRequest(
            @NotBlank @Size(max = 100) String firstName,
            @NotBlank @Size(max = 100) String lastName,
            @NotBlank @Size(max = 40) String phone,
            @Size(max = 40) String whatsapp,
            @Email @Size(max = 180) String email,
            @Size(max = 30) String document,
            @Size(max = 100) String acquisitionSource,
            UUID referredByCustomerId,
            @Size(max = 2000) String notes,
            @NotNull @Valid AddressRequest primaryAddress,
            @Valid PreferenceRequest preferences
    ) {}

    public record AddressRequest(
            @NotNull UUID zoneId,
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 30) String streetNumber,
            @Size(max = 100) String neighborhood,
            @NotBlank @Size(max = 120) String locality,
            @Size(max = 2000) String references,
            BigDecimal latitude,
            BigDecimal longitude
    ) {}

    public record PreferenceRequest(
            @Size(max = 80) String fragrance,
            @Size(max = 30) String fragranceIntensity,
            @Size(max = 80) String soapType,
            Boolean softenerAllowed,
            @Size(max = 2000) String allergyNotes,
            Boolean babyClothes,
            Boolean dryerAllowed,
            Integer maxTemperatureCelsius,
            Boolean colorMixAllowed,
            Boolean exclusiveCycle,
            Boolean stainTreatment,
            @Size(max = 40) String preferredPaymentMethod
    ) {}

    public record CustomerSummary(
            UUID id,
            String firstName,
            String lastName,
            String phone,
            String whatsapp,
            String email,
            String status,
            AddressResponse primaryAddress
    ) {}

    public record CustomerResponse(
            UUID id,
            String firstName,
            String lastName,
            String phone,
            String whatsapp,
            String email,
            String document,
            String status,
            String acquisitionSource,
            String notes,
            List<AddressResponse> addresses,
            PreferenceResponse preferences
    ) {}

    public record AddressResponse(
            UUID id,
            UUID zoneId,
            String zoneName,
            String street,
            String streetNumber,
            String neighborhood,
            String locality,
            String references,
            boolean primary
    ) {}

    public record PreferenceResponse(
            String fragrance,
            String fragranceIntensity,
            String soapType,
            boolean softenerAllowed,
            String allergyNotes,
            boolean babyClothes,
            boolean dryerAllowed,
            Integer maxTemperatureCelsius,
            boolean colorMixAllowed,
            boolean exclusiveCycle,
            boolean stainTreatment,
            String preferredPaymentMethod
    ) {}
}
