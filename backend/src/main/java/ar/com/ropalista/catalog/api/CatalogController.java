package ar.com.ropalista.catalog.api;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.catalog.persistence.GarmentEquivalenceRepository;
import ar.com.ropalista.catalog.persistence.ServiceOfferingRepository;
import ar.com.ropalista.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/catalog")
public class CatalogController {
    private final GarmentEquivalenceRepository equivalences;
    private final ServiceOfferingRepository services;

    public CatalogController(GarmentEquivalenceRepository equivalences, ServiceOfferingRepository services) {
        this.equivalences = equivalences;
        this.services = services;
    }

    @GetMapping("/equivalences")
    ApiResponse<List<EquivalenceResponse>> equivalences() {
        return ApiResponse.ok(equivalences.findAllApplicable(LocalDate.now()).stream()
                .map(e -> new EquivalenceResponse(e.getCode(), e.getName(), e.getCategory(),
                        e.getPhysicalUnitsPerGroup(), e.getEquivalentUnits(), e.getEstimatedWeightGrams(),
                        e.isDryerAllowed(), e.isExclusiveCycleRequired(), e.isQuoteRequired()))
                .toList());
    }

    @GetMapping("/services")
    ApiResponse<List<ServiceResponse>> services() {
        var latestByCode = services.findAllApplicable(LocalDate.now()).stream()
                .collect(Collectors.toMap(ServiceOffering::getCode, Function.identity(),
                        (latest, ignored) -> latest, LinkedHashMap::new));
        return ApiResponse.ok(latestByCode.values().stream()
                .map(service -> new ServiceResponse(service.getCode(), service.getName(), service.getDescription(),
                        service.getMaxEquivalentUnits(), service.getMaxWeightGrams(), service.getSafeCapacityGrams(),
                        service.isRequiresQuote()))
                .toList());
    }

    public record EquivalenceResponse(String code, String name, String category, int physicalUnitsPerGroup,
                                      BigDecimal equivalentUnits, Integer estimatedWeightGrams,
                                      boolean dryerAllowed, boolean exclusiveCycleRequired, boolean quoteRequired) {}

    public record ServiceResponse(String code, String name, String description, BigDecimal maxEquivalentUnits,
                                  Integer maxWeightGrams, Integer safeCapacityGrams, boolean requiresQuote) {}
}
