package com.fourbubbles.ropalista.garment.api;

import com.fourbubbles.ropalista.common.api.ApiResponse;
import com.fourbubbles.ropalista.garment.infrastructure.GarmentEquivalenceRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/garment-equivalences")
public class GarmentEquivalenceController {
    private final GarmentEquivalenceRepository repository;

    public GarmentEquivalenceController(GarmentEquivalenceRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    ApiResponse<List<Response>> list() {
        return ApiResponse.of(repository.findByActiveTrueAndDeletedAtIsNullOrderByCategoryAscNameAsc().stream()
                .map(item -> new Response(item.getId(), item.getCode(), item.getName(), item.getCategory(),
                        item.getPhysicalUnitsPerGroup(), item.getEquivalentUnits(), item.getEstimatedWeightGrams(),
                        item.isCommonWashAllowed(), item.isDryerAllowed(), item.isExclusiveCycleRequired(),
                        item.isQuoteRequired()))
                .toList());
    }

    public record Response(UUID id, String code, String name, String category, int physicalUnitsPerGroup,
                           BigDecimal equivalentUnits, Integer estimatedWeightGrams, boolean commonWashAllowed,
                           boolean dryerAllowed, boolean exclusiveCycleRequired, boolean quoteRequired) {}
}
