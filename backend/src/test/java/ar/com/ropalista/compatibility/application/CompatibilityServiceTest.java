package ar.com.ropalista.compatibility.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.compatibility.api.CompatibilityDtos;
import ar.com.ropalista.compatibility.domain.ColorGroup;
import ar.com.ropalista.compatibility.domain.FragrancePolicy;
import ar.com.ropalista.compatibility.domain.MaterialGroup;
import ar.com.ropalista.compatibility.domain.OrderTreatmentProfile;
import ar.com.ropalista.compatibility.persistence.CompatibilityEvaluationRepository;
import ar.com.ropalista.compatibility.persistence.OrderTreatmentProfileRepository;
import ar.com.ropalista.customer.domain.Client;
import ar.com.ropalista.order.domain.LaundryOrder;
import ar.com.ropalista.order.domain.OrderStatus;
import ar.com.ropalista.order.persistence.LaundryOrderRepository;
import ar.com.ropalista.reception.domain.OrderReception;
import ar.com.ropalista.reception.persistence.OrderReceptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompatibilityServiceTest {
    @Mock
    private OrderTreatmentProfileRepository profiles;
    @Mock
    private CompatibilityEvaluationRepository evaluations;
    @Mock
    private LaundryOrderRepository orders;
    @Mock
    private OrderReceptionRepository receptions;
    @Mock
    private CompatibilityEngine engine;
    @Mock
    private AuditService audit;
    @Mock
    private LaundryOrder order;
    @Mock
    private Client client;
    @Mock
    private OrderReception reception;

    @Test
    void clientAndOrderRestrictionsCannotBeRelaxedByTheProfileRequest() {
        UUID orderId = UUID.randomUUID();
        UUID receptionId = UUID.randomUUID();
        when(order.getStatus()).thenReturn(OrderStatus.CLASSIFIED);
        when(order.getClient()).thenReturn(client);
        when(order.isExclusiveCycle()).thenReturn(true);
        when(order.getId()).thenReturn(orderId);
        when(client.getPreferencesJson()).thenReturn("""
                {
                  "dryerAllowed": false,
                  "softenerAllowed": false,
                  "hypoallergenic": true
                }
                """);
        when(reception.getId()).thenReturn(receptionId);
        when(orders.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));
        when(receptions.findByOrderId(orderId)).thenReturn(Optional.of(reception));
        when(profiles.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(profiles.saveAndFlush(any(OrderTreatmentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CompatibilityService service = new CompatibilityService(profiles, evaluations, orders, receptions,
                engine, new ObjectMapper(), audit);
        var request = new CompatibilityDtos.TreatmentProfileRequest(
                ColorGroup.LIGHT, MaterialGroup.COTTON, 40, true,
                FragrancePolicy.STANDARD, true, false, false,
                false, false, false, null);

        var response = service.saveProfile(orderId, request);

        ArgumentCaptor<OrderTreatmentProfile> captor = ArgumentCaptor.forClass(OrderTreatmentProfile.class);
        org.mockito.Mockito.verify(profiles).saveAndFlush(captor.capture());
        OrderTreatmentProfile saved = captor.getValue();
        assertThat(saved.isDryerAllowed()).isFalse();
        assertThat(saved.isSoftenerAllowed()).isFalse();
        assertThat(saved.isHypoallergenic()).isTrue();
        assertThat(saved.isExclusiveCycle()).isTrue();
        assertThat(response.dryerAllowed()).isFalse();
        assertThat(response.softenerAllowed()).isFalse();
        assertThat(response.hypoallergenic()).isTrue();
        assertThat(response.exclusiveCycle()).isTrue();
    }
}
