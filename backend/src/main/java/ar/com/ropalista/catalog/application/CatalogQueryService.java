package ar.com.ropalista.catalog.application;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.catalog.persistence.GarmentEquivalenceRepository;
import ar.com.ropalista.catalog.persistence.ServiceOfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CatalogQueryService {
    private final GarmentEquivalenceRepository equivalences;
    private final ServiceOfferingRepository services;

    public CatalogQueryService(GarmentEquivalenceRepository equivalences,
                               ServiceOfferingRepository services) {
        this.equivalences = equivalences;
        this.services = services;
    }

    @Transactional(readOnly = true)
    public List<EquivalenceView> equivalences() {
        return equivalences.findAllApplicable(LocalDate.now()).stream()
                .map(value -> new EquivalenceView(
                        value.getCode(),
                        value.getName(),
                        value.getCategory(),
                        value.getPhysicalUnitsPerGroup(),
                        value.getEquivalentUnits(),
                        value.getEstimatedWeightGrams(),
                        value.isDryerAllowed(),
                        value.isExclusiveCycleRequired(),
                        value.isQuoteRequired()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceView> services() {
        var latestByCode = services.findAllApplicable(LocalDate.now()).stream()
                .collect(Collectors.toMap(
                        ServiceOffering::getCode,
                        Function.identity(),
                        (latest, ignored) -> latest,
                        LinkedHashMap::new));
        return latestByCode.values().stream()
                .map(value -> new ServiceView(
                        value.getCode(),
                        value.getName(),
                        value.getDescription(),
                        value.getMaxEquivalentUnits(),
                        value.getMaxWeightGrams(),
                        value.getSafeCapacityGrams(),
                        value.isRequiresQuote()))
                .toList();
    }

    public record EquivalenceView(
            String code,
            String name,
            String category,
            int physicalUnitsPerGroup,
            BigDecimal equivalentUnits,
            Integer estimatedWeightGrams,
            boolean dryerAllowed,
            boolean exclusiveCycleRequired,
            boolean quoteRequired) {}

    public record ServiceView(
            String code,
            String name,
            String description,
            BigDecimal maxEquivalentUnits,
            Integer maxWeightGrams,
            Integer safeCapacityGrams,
            boolean requiresQuote) {}
}
