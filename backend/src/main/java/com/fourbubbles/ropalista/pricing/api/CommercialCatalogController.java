package com.fourbubbles.ropalista.pricing.api;

import com.fourbubbles.ropalista.common.api.ApiResponse;
import com.fourbubbles.ropalista.pricing.infrastructure.ServicePlanRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-plans")
public class CommercialCatalogController {
    private final ServicePlanRepository plans;

    public CommercialCatalogController(ServicePlanRepository plans) {
        this.plans = plans;
    }

    @GetMapping
    ApiResponse<List<Response>> list() {
        return ApiResponse.of(plans.findByActiveTrueAndDeletedAtIsNullOrderByName().stream()
                .map(plan -> new Response(plan.getId(), plan.getCode(), plan.getName(), plan.getDescription(),
                        plan.getMaxEquivalentUnits(), plan.getMaxWeightGrams()))
                .toList());
    }

    public record Response(UUID id, String code, String name, String description,
                           BigDecimal maxEquivalentUnits, Integer maxWeightGrams) {}
}
