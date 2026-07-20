package ar.com.ropalista.customer.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.customer.api.ClientDtos;
import ar.com.ropalista.customer.domain.Address;
import ar.com.ropalista.customer.domain.Client;
import ar.com.ropalista.customer.persistence.ClientRepository;
import ar.com.ropalista.location.persistence.ZoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ClientService {
    private final ClientRepository clients;
    private final ZoneRepository zones;
    private final AuditService audit;

    public ClientService(ClientRepository clients, ZoneRepository zones, AuditService audit) {
        this.clients = clients;
        this.zones = zones;
        this.audit = audit;
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
                request.email(), request.acquisitionSource(), request.preferencesJson(), request.notes());
        for (var addressRequest : request.addresses()) {
            var zone = zones.findByCodeAndActiveTrue(addressRequest.zoneCode())
                    .orElseThrow(() -> new BusinessException("ZONE_NOT_AVAILABLE", "La zona indicada no está habilitada", HttpStatus.UNPROCESSABLE_ENTITY));
            client.addAddress(new Address(zone, addressRequest.street(), addressRequest.number(), addressRequest.extra(),
                    addressRequest.locality(), addressRequest.neighborhood(), addressRequest.references(), addressRequest.primaryAddress()));
        }
        Client saved = clients.save(client);
        audit.record("CLIENT", saved.getId(), "CREATE", null, summary(saved), "Alta de cliente");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ClientDtos.ClientResponse get(UUID id) {
        return toResponse(clients.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException("CLIENT_NOT_FOUND", "Cliente inexistente", HttpStatus.NOT_FOUND)));
    }

    @Transactional(readOnly = true)
    public Page<ClientDtos.ClientResponse> search(String lastName, int page, int size) {
        return clients.findByDeletedAtIsNullAndLastNameContainingIgnoreCase(lastName == null ? "" : lastName,
                PageRequest.of(page, Math.min(size, 100))).map(this::toResponse);
    }

    private ClientDtos.ClientResponse toResponse(Client client) {
        return new ClientDtos.ClientResponse(client.getId(), client.getFirstName(), client.getLastName(), client.getPhone(),
                client.getWhatsapp(), client.getEmail(), client.getStatus().name(), client.getAcquisitionSource(),
                client.getPreferencesJson(), client.getNotes(), client.getAddresses().stream().filter(Address::isActive)
                .map(a -> new ClientDtos.AddressResponse(a.getId(), a.getZone().getCode(), a.getZone().getName(),
                        a.getStreet(), a.getNumber(), a.getExtra(), a.getLocality(), a.getNeighborhood(),
                        a.getReferences(), a.isPrimaryAddress())).toList());
    }

    private Object summary(Client client) {
        return java.util.Map.of("id", client.getId(), "whatsapp", client.getWhatsapp(), "status", client.getStatus().name());
    }
}
