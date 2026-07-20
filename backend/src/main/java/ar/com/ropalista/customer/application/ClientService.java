package ar.com.ropalista.customer.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.customer.api.ClientDtos;
import ar.com.ropalista.customer.domain.Address;
import ar.com.ropalista.customer.domain.Client;
import ar.com.ropalista.customer.persistence.AddressRepository;
import ar.com.ropalista.customer.persistence.ClientRepository;
import ar.com.ropalista.location.persistence.ZoneRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.UUID;

@Service
public class ClientService {
    private final ClientRepository clients;
    private final AddressRepository addresses;
    private final ZoneRepository zones;
    private final AuditService audit;
    private final ObjectMapper objectMapper;

    public ClientService(ClientRepository clients, AddressRepository addresses, ZoneRepository zones,
                         AuditService audit, ObjectMapper objectMapper) {
        this.clients = clients;
        this.addresses = addresses;
        this.zones = zones;
        this.audit = audit;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ClientDtos.ClientResponse create(ClientDtos.CreateClientRequest request) {
        if (clients.existsByWhatsappAndDeletedAtIsNull(request.whatsapp())) {
            throw new BusinessException("DUPLICATE_WHATSAPP", "Ya existe un cliente activo con ese WhatsApp", HttpStatus.CONFLICT);
        }
        long primaryCount = request.addresses().stream().filter(ClientDtos.AddressRequest::primaryAddress).count();
        if (primaryCount != 1) {
            throw new BusinessException("INVALID_PRIMARY_ADDRESS", "Debe existir exactamente un domicilio principal", HttpStatus.BAD_REQUEST);
        }
        Client client = new Client(request.firstName(), request.lastName(), request.phone(), request.whatsapp(),
                request.email(), request.acquisitionSource(), resolvePreferences(request.preferences(), request.preferencesJson()), request.notes());
        for (var addressRequest : request.addresses()) {
            client.addAddress(toAddress(addressRequest));
        }
        Client saved = clients.save(client);
        audit.record("CLIENT", saved.getId(), "CREATE", null, summary(saved), "Alta de cliente");
        return toResponse(saved);
    }

    @Transactional
    public ClientDtos.ClientResponse update(UUID id, ClientDtos.UpdateClientRequest request) {
        Client client = find(id);
        if (clients.existsByWhatsappAndDeletedAtIsNullAndIdNot(request.whatsapp(), id)) {
            throw new BusinessException("DUPLICATE_WHATSAPP", "Ya existe otro cliente activo con ese WhatsApp", HttpStatus.CONFLICT);
        }
        Object before = summary(client);
        client.updateProfile(request.firstName(), request.lastName(), request.phone(), request.whatsapp(),
                request.email(), request.status(), request.acquisitionSource(),
                resolvePreferences(request.preferences(), request.preferencesJson()), request.notes());
        audit.record("CLIENT", client.getId(), "UPDATE", before, summary(client), "Actualización de cliente");
        return toResponse(client);
    }

    @Transactional
    public ClientDtos.ClientResponse addAddress(UUID clientId, ClientDtos.AddressRequest request) {
        Client client = find(clientId);
        var activeAddresses = addresses.findByClientIdAndActiveTrueOrderByPrimaryAddressDescValidFromDesc(clientId);
        if (activeAddresses.isEmpty() && !request.primaryAddress()) {
            throw new BusinessException("PRIMARY_ADDRESS_REQUIRED", "El primer domicilio activo debe ser principal", HttpStatus.BAD_REQUEST);
        }
        if (request.primaryAddress()) {
            activeAddresses.stream().filter(Address::isPrimaryAddress).forEach(Address::demotePrimary);
        }
        Address address = toAddress(request);
        client.addAddress(address);
        clients.save(client);
        audit.record("CLIENT_ADDRESS", address.getId(), "CREATE", null, addressSummary(address), "Alta de domicilio");
        return toResponse(client);
    }

    @Transactional
    public ClientDtos.ClientResponse makePrimary(UUID clientId, UUID addressId) {
        Client client = find(clientId);
        Address target = findActiveAddress(clientId, addressId);
        Object before = addressSummary(target);
        addresses.findByClientIdAndActiveTrueOrderByPrimaryAddressDescValidFromDesc(clientId)
                .stream().filter(Address::isPrimaryAddress).forEach(Address::demotePrimary);
        target.makePrimary();
        audit.record("CLIENT_ADDRESS", target.getId(), "MAKE_PRIMARY", before, addressSummary(target),
                "Cambio de domicilio principal");
        return toResponse(client);
    }

    @Transactional
    public ClientDtos.ClientResponse deactivateAddress(UUID clientId, UUID addressId) {
        Client client = find(clientId);
        Address target = findActiveAddress(clientId, addressId);
        if (target.isPrimaryAddress()) {
            throw new BusinessException("PRIMARY_ADDRESS_CANNOT_BE_DEACTIVATED",
                    "Seleccione otro domicilio principal antes de desactivar este domicilio", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (addresses.countByClientIdAndActiveTrue(clientId) <= 1) {
            throw new BusinessException("LAST_ADDRESS_CANNOT_BE_DEACTIVATED",
                    "El cliente debe conservar al menos un domicilio activo", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        Object before = addressSummary(target);
        target.deactivate(OffsetDateTime.now());
        audit.record("CLIENT_ADDRESS", target.getId(), "DEACTIVATE", before, addressSummary(target),
                "Baja lógica de domicilio");
        return toResponse(client);
    }

    @Transactional(readOnly = true)
    public ClientDtos.ClientResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional(readOnly = true)
    public Page<ClientDtos.ClientResponse> search(String lastName, int page, int size) {
        return clients.findByDeletedAtIsNullAndLastNameContainingIgnoreCase(lastName == null ? "" : lastName,
                PageRequest.of(Math.max(page, 0), Math.max(1, Math.min(size, 100)))).map(this::toResponse);
    }

    private Client find(UUID id) {
        return clients.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND", "Cliente inexistente", HttpStatus.NOT_FOUND));
    }

    private Address findActiveAddress(UUID clientId, UUID addressId) {
        return addresses.findByIdAndClientIdAndActiveTrue(addressId, clientId)
                .orElseThrow(() -> new BusinessException("ADDRESS_NOT_FOUND", "Domicilio activo inexistente", HttpStatus.NOT_FOUND));
    }

    private Address toAddress(ClientDtos.AddressRequest request) {
        var zone = zones.findByCodeAndActiveTrue(request.zoneCode())
                .orElseThrow(() -> new BusinessException("ZONE_NOT_AVAILABLE", "La zona indicada no está habilitada", HttpStatus.UNPROCESSABLE_ENTITY));
        return new Address(zone, request.street(), request.number(), request.extra(), request.locality(),
                request.neighborhood(), request.references(), request.primaryAddress());
    }

    private ClientDtos.ClientResponse toResponse(Client client) {
        var active = client.getAddresses().stream().filter(Address::isActive)
                .sorted(Comparator.comparing(Address::isPrimaryAddress).reversed()
                        .thenComparing(Address::getValidFrom, Comparator.reverseOrder()))
                .map(this::toAddressResponse).toList();
        var history = client.getAddresses().stream().filter(address -> !address.isActive())
                .sorted(Comparator.comparing(Address::getValidTo, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toAddressResponse).toList();
        return new ClientDtos.ClientResponse(client.getId(), client.getFirstName(), client.getLastName(), client.getPhone(),
                client.getWhatsapp(), client.getEmail(), client.getStatus().name(), client.getAcquisitionSource(),
                client.getPreferencesJson(), parsePreferences(client.getPreferencesJson()), client.getNotes(), active, history);
    }

    private ClientDtos.AddressResponse toAddressResponse(Address address) {
        return new ClientDtos.AddressResponse(address.getId(), address.getZone().getCode(), address.getZone().getName(),
                address.getStreet(), address.getNumber(), address.getExtra(), address.getLocality(), address.getNeighborhood(),
                address.getReferences(), address.isPrimaryAddress(), address.isActive(), address.getValidFrom(), address.getValidTo());
    }

    private String resolvePreferences(ClientDtos.ClientPreferencesRequest typed, String legacyJson) {
        if (typed != null) {
            try {
                return objectMapper.writeValueAsString(typed);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("No se pudieron serializar las preferencias", ex);
            }
        }
        if (legacyJson == null || legacyJson.isBlank()) return "{}";
        try {
            JsonNode node = objectMapper.readTree(legacyJson);
            if (node == null || !node.isObject()) throw invalidPreferences();
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw invalidPreferences();
        }
    }

    private ClientDtos.ClientPreferencesResponse parsePreferences(String json) {
        try {
            JsonNode node = objectMapper.readTree(json == null || json.isBlank() ? "{}" : json);
            return new ClientDtos.ClientPreferencesResponse(text(node, "fragrance"), bool(node, "softenerAllowed"),
                    bool(node, "dryerAllowed"), bool(node, "hypoallergenic"), bool(node, "separateColors"),
                    text(node, "specialInstructions"));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Las preferencias persistidas no contienen JSON válido", ex);
        }
    }

    private String text(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        return value == null || value.isNull() ? null : value.asText();
    }

    private Boolean bool(JsonNode node, String name) {
        JsonNode value = node == null ? null : node.get(name);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private BusinessException invalidPreferences() {
        return new BusinessException("INVALID_PREFERENCES", "Las preferencias deben ser un objeto JSON válido", HttpStatus.BAD_REQUEST);
    }

    private Object summary(Client client) {
        return java.util.Map.of("id", client.getId(), "whatsapp", client.getWhatsapp(), "status", client.getStatus().name());
    }

    private Object addressSummary(Address address) {
        return java.util.Map.of("id", address.getId(), "active", address.isActive(),
                "primary", address.isPrimaryAddress(), "street", address.getStreet(), "number", address.getNumber());
    }
}
