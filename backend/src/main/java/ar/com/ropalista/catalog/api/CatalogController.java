package ar.com.ropalista.catalog.api;

import ar.com.ropalista.catalog.persistence.GarmentEquivalenceRepository;
import ar.com.ropalista.common.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {
    private final GarmentEquivalenceRepository equivalences;

    public CatalogController(GarmentEquivalenceRepository equivalences) {
        this.equivalences = equivalences;
    }

    @GetMapping("/equivalences")
    ApiResponse<List<EquivalenceResponse>> equivalences() {
        return ApiResponse.ok(equivalences.findAllApplicable(LocalDate.now()).stream()
                .map(e -> new EquivalenceResponse(e.getCode(), e.getName(), e.getCategory(),
                        e.getPhysicalUnitsPerGroup(), e.getEquivalentUnits(), e.getEstimatedWeightGrams(),
                        e.isDryerAllowed(), e.isExclusiveCycleRequired(), e.isQuoteRequired()))
                .toList());
    }

    public record EquivalenceResponse(String code, String name, String category, int physicalUnitsPerGroup,
                                      BigDecimal equivalentUnits, Integer estimatedWeightGrams,
                                      boolean dryerAllowed, boolean exclusiveCycleRequired, boolean quoteRequired) {}
}
