package com.fourbubbles.ropalista.customer.application;

import com.fourbubbles.ropalista.common.application.ResourceNotFoundException;
import com.fourbubbles.ropalista.customer.api.CustomerDtos;
import com.fourbubbles.ropalista.customer.domain.Address;
import com.fourbubbles.ropalista.customer.domain.Customer;
import com.fourbubbles.ropalista.customer.domain.CustomerPreference;
import com.fourbubbles.ropalista.customer.infrastructure.AddressRepository;
import com.fourbubbles.ropalista.customer.infrastructure.CustomerPreferenceRepository;
import com.fourbubbles.ropalista.customer.infrastructure.CustomerRepository;
import com.fourbubbles.ropalista.location.domain.Zone;
import com.fourbubbles.ropalista.location.infrastructure.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final AddressRepository addresses;
    private final CustomerPreferenceRepository preferences;
    private final ZoneRepository zones;

    public CustomerService(CustomerRepository customers, AddressRepository addresses,
                           CustomerPreferenceRepository preferences, ZoneRepository zones) {
        this.customers = customers;
        this.addresses = addresses;
        this.preferences = preferences;
        this.zones = zones;
    }

    @Transactional
    public CustomerDtos.CustomerResponse create(CustomerDtos.CreateCustomerRequest request) {
        Zone zone = zones.findById(request.primaryAddress().zoneId())
                .filter(value -> value.isActive() && !value.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Zona activa", request.primaryAddress().zoneId()));

        Customer customer = new Customer();
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setPhone(request.phone().trim());
        customer.setWhatsapp(blankToNull(request.whatsapp()));
        customer.setEmail(blankToNull(request.email()));
        customer.setDocument(blankToNull(request.document()));
        customer.setAcquisitionSource(blankToNull(request.acquisitionSource()));
        customer.setReferredByCustomerId(request.referredByCustomerId());
        customer.setNotes(blankToNull(request.notes()));
        customers.save(customer);

        Address address = new Address();
        address.setCustomer(customer);
        address.setZone(zone);
        address.setStreet(request.primaryAddress().street().trim());
        address.setStreetNumber(request.primaryAddress().streetNumber().trim());
        address.setNeighborhood(blankToNull(request.primaryAddress().neighborhood()));
        address.setLocality(request.primaryAddress().locality().trim());
        address.setReferences(blankToNull(request.primaryAddress().references()));
        address.setLatitude(request.primaryAddress().latitude());
        address.setLongitude(request.primaryAddress().longitude());
        address.setPrimary(true);
        addresses.save(address);

        if (request.preferences() != null) {
            CustomerPreference preference = toPreference(customer, request.preferences());
            preferences.save(preference);
        }

        return get(customer.getId());
    }

    @Transactional(readOnly = true)
    public List<CustomerDtos.CustomerSummary> list() {
        return customers.findByDeletedAtIsNullOrderByLastNameAscFirstNameAsc().stream()
                .map(customer -> {
                    Address primary = addresses.findByCustomerIdAndDeletedAtIsNullOrderByPrimaryDesc(customer.getId())
                            .stream().findFirst().orElse(null);
                    return new CustomerDtos.CustomerSummary(customer.getId(), customer.getFirstName(),
                            customer.getLastName(), customer.getPhone(), customer.getWhatsapp(), customer.getEmail(),
                            customer.getStatus().name(), primary == null ? null : address(primary));
                }).toList();
    }

    @Transactional(readOnly = true)
    public CustomerDtos.CustomerResponse get(UUID id) {
        Customer customer = customers.findById(id).filter(value -> !value.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        List<CustomerDtos.AddressResponse> customerAddresses =
                addresses.findByCustomerIdAndDeletedAtIsNullOrderByPrimaryDesc(id).stream()
                        .map(this::address).toList();
        CustomerDtos.PreferenceResponse preference = preferences.findByCustomerIdAndDeletedAtIsNull(id)
                .map(this::preference).orElse(null);
        return new CustomerDtos.CustomerResponse(customer.getId(), customer.getFirstName(), customer.getLastName(),
                customer.getPhone(), customer.getWhatsapp(), customer.getEmail(), customer.getDocument(),
                customer.getStatus().name(), customer.getAcquisitionSource(), customer.getNotes(),
                customerAddresses, preference);
    }

    private CustomerPreference toPreference(Customer customer, CustomerDtos.PreferenceRequest request) {
        CustomerPreference value = new CustomerPreference();
        value.setCustomer(customer);
        value.setFragrance(blankToNull(request.fragrance()));
        value.setFragranceIntensity(blankToNull(request.fragranceIntensity()));
        value.setSoapType(blankToNull(request.soapType()));
        value.setSoftenerAllowed(defaultBoolean(request.softenerAllowed(), true));
        value.setAllergyNotes(blankToNull(request.allergyNotes()));
        value.setBabyClothes(defaultBoolean(request.babyClothes(), false));
        value.setDryerAllowed(defaultBoolean(request.dryerAllowed(), true));
        value.setMaxTemperatureCelsius(request.maxTemperatureCelsius());
        value.setColorMixAllowed(defaultBoolean(request.colorMixAllowed(), false));
        value.setExclusiveCycle(defaultBoolean(request.exclusiveCycle(), false));
        value.setStainTreatment(defaultBoolean(request.stainTreatment(), false));
        value.setPreferredPaymentMethod(blankToNull(request.preferredPaymentMethod()));
        return value;
    }

    private CustomerDtos.AddressResponse address(Address address) {
        return new CustomerDtos.AddressResponse(address.getId(), address.getZone().getId(),
                address.getZone().getName(), address.getStreet(), address.getStreetNumber(),
                address.getNeighborhood(), address.getLocality(), address.getReferences(), address.isPrimary());
    }

    private CustomerDtos.PreferenceResponse preference(CustomerPreference value) {
        return new CustomerDtos.PreferenceResponse(value.getFragrance(), value.getFragranceIntensity(),
                value.getSoapType(), value.isSoftenerAllowed(), value.getAllergyNotes(), value.isBabyClothes(),
                value.isDryerAllowed(), value.getMaxTemperatureCelsius(), value.isColorMixAllowed(),
                value.isExclusiveCycle(), value.isStainTreatment(), value.getPreferredPaymentMethod());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean defaultBoolean(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }
}
