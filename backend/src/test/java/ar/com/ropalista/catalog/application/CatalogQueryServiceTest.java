package ar.com.ropalista.catalog.application;

import ar.com.ropalista.catalog.domain.GarmentEquivalence;
import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.catalog.persistence.GarmentEquivalenceRepository;
import ar.com.ropalista.catalog.persistence.ServiceOfferingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CatalogQueryServiceTest {
    private GarmentEquivalenceRepository equivalences;
    private ServiceOfferingRepository services;
    private CatalogQueryService queryService;

    @BeforeEach
    void setUp() {
        equivalences = mock(GarmentEquivalenceRepository.class);
        services = mock(ServiceOfferingRepository.class);
        queryService = new CatalogQueryService(equivalences, services);
    }

    @Test
    void mapsApplicableEquivalencesWithoutLosingOperationalFields() {
        GarmentEquivalence value = mock(GarmentEquivalence.class);
        when(value.getCode()).thenReturn("TSHIRT");
        when(value.getName()).thenReturn("Remera");
        when(value.getCategory()).thenReturn("ROPA");
        when(value.getPhysicalUnitsPerGroup()).thenReturn(2);
        when(value.getEquivalentUnits()).thenReturn(new BigDecimal("1.50"));
        when(value.getEstimatedWeightGrams()).thenReturn(500);
        when(value.isDryerAllowed()).thenReturn(true);
        when(value.isExclusiveCycleRequired()).thenReturn(false);
        when(value.isQuoteRequired()).thenReturn(false);
        when(equivalences.findAllApplicable(any(LocalDate.class))).thenReturn(List.of(value));

        var result = queryService.equivalences();

        assertThat(result).containsExactly(new CatalogQueryService.EquivalenceView(
                "TSHIRT", "Remera", "ROPA", 2, new BigDecimal("1.50"),
                500, true, false, false));
        verify(equivalences).findAllApplicable(any(LocalDate.class));
    }

    @Test
    void keepsFirstApplicableServiceForEachCodeAndPreservesOrder() {
        ServiceOffering first = service("ROPA_LISTA_12", "Ropa lista 12", "Actual");
        ServiceOffering duplicate = service("ROPA_LISTA_12", "Ropa lista 12", "Anterior");
        ServiceOffering second = service("PLANCHADO", "Planchado", "Vigente");
        when(services.findAllApplicable(any(LocalDate.class)))
                .thenReturn(List.of(first, duplicate, second));

        var result = queryService.services();

        assertThat(result).extracting(CatalogQueryService.ServiceView::code)
                .containsExactly("ROPA_LISTA_12", "PLANCHADO");
        assertThat(result.getFirst().description()).isEqualTo("Actual");
        verify(services).findAllApplicable(any(LocalDate.class));
    }

    private ServiceOffering service(String code, String name, String description) {
        ServiceOffering value = mock(ServiceOffering.class);
        when(value.getCode()).thenReturn(code);
        when(value.getName()).thenReturn(name);
        when(value.getDescription()).thenReturn(description);
        when(value.getMaxEquivalentUnits()).thenReturn(new BigDecimal("12.00"));
        when(value.getMaxWeightGrams()).thenReturn(10000);
        when(value.getSafeCapacityGrams()).thenReturn(9000);
        when(value.isRequiresQuote()).thenReturn(false);
        return value;
    }
}
