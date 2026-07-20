package ar.com.ropalista.pricing.application;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.customer.domain.Address;
import ar.com.ropalista.customer.domain.Client;
import ar.com.ropalista.location.domain.Zone;
import ar.com.ropalista.pricing.domain.PriceDefinition;
import ar.com.ropalista.pricing.domain.Promotion;
import ar.com.ropalista.pricing.persistence.PriceDefinitionRepository;
import ar.com.ropalista.pricing.persistence.PromotionRepository;
import ar.com.ropalista.pricing.persistence.PromotionUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PricingServiceTest {
    private PriceDefinitionRepository prices;
    private PromotionRepository promotions;
    private PromotionUsageRepository usages;
    private PricingService service;
    private ServiceOffering offering;
    private Client client;
    private Address address;

    @BeforeEach
    void setUp() {
        prices = mock(PriceDefinitionRepository.class);
        promotions = mock(PromotionRepository.class);
        usages = mock(PromotionUsageRepository.class);
        service = new PricingService(prices, promotions, usages);

        offering = mock(ServiceOffering.class);
        client = mock(Client.class);
        address = mock(Address.class);
        Zone zone = mock(Zone.class);
        PriceDefinition price = mock(PriceDefinition.class);
        when(offering.getId()).thenReturn(UUID.randomUUID());
        when(offering.getCode()).thenReturn("ROPA_LISTA_12");
        when(offering.getName()).thenReturn("Ropa Lista 12");
        when(address.getZone()).thenReturn(zone);
        when(zone.getId()).thenReturn(UUID.randomUUID());
        when(price.getAmount()).thenReturn(new BigDecimal("6500"));
        when(prices.findApplicable(any(), any(), any())).thenReturn(List.of(price));
    }

    @Test
    void preservesExplainableBasePriceWithoutPromotion() {
        var quote = service.quote(offering, client, address, null, true, OffsetDateTime.now());

        assertThat(quote.total()).isEqualByComparingTo("6500.00");
        assertThat(quote.breakdown()).extracting(PricingService.BreakdownLine::code).containsExactly("BASE_PRICE");
    }

    @Test
    void appliesAutomaticFirstTrialFixedPrice() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.isAutomaticApplicable()).thenReturn(true);
        when(promotion.getApplicableServiceCode()).thenReturn("ROPA_LISTA_12");
        when(promotion.getValidFrom()).thenReturn(OffsetDateTime.now().minusDays(1));
        when(promotion.getValidTo()).thenReturn(null);
        when(promotion.isNewCustomersOnly()).thenReturn(true);
        when(promotion.isOnePerAddress()).thenReturn(true);
        when(promotion.getFixedPrice()).thenReturn(new BigDecimal("5500"));
        when(promotion.getName()).thenReturn("Primera prueba");
        when(promotion.getId()).thenReturn(UUID.randomUUID());
        when(promotions.findActive(org.mockito.ArgumentMatchers.eq("FIRST_TRIAL"), any())).thenReturn(Optional.of(promotion));

        var quote = service.quote(offering, client, address, "FIRST_TRIAL", true, OffsetDateTime.now());

        assertThat(quote.total()).isEqualByComparingTo("5500.00");
        assertThat(quote.discount()).isEqualByComparingTo("1000.00");
    }

    @Test
    void rejectsFirstPurchasePromotionForExistingClient() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.isAutomaticApplicable()).thenReturn(true);
        when(promotion.getApplicableServiceCode()).thenReturn("ROPA_LISTA_12");
        when(promotion.getValidFrom()).thenReturn(OffsetDateTime.now().minusDays(1));
        when(promotion.isNewCustomersOnly()).thenReturn(true);
        when(promotions.findActive(org.mockito.ArgumentMatchers.eq("FIRST_TRIAL"), any())).thenReturn(Optional.of(promotion));

        assertThatThrownBy(() -> service.quote(offering, client, address, "FIRST_TRIAL", false, OffsetDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("primera compra");
    }

    @Test
    void rejectsPromotionThatRequiresManualConditions() {
        Promotion promotion = mock(Promotion.class);
        when(promotion.isAutomaticApplicable()).thenReturn(false);
        when(promotions.findActive(org.mockito.ArgumentMatchers.eq("FULL_ROUTE"), any())).thenReturn(Optional.of(promotion));

        assertThatThrownBy(() -> service.quote(offering, client, address, "FULL_ROUTE", true, OffsetDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("validación manual");
    }
}
