package com.fourbubbles.ropalista.location.api;

import com.fourbubbles.ropalista.common.api.ApiResponse;
import com.fourbubbles.ropalista.location.infrastructure.ZoneRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/zones")
public class ZoneController {
    private final ZoneRepository zones;

    public ZoneController(ZoneRepository zones) {
        this.zones = zones;
    }

    @GetMapping
    ApiResponse<List<ZoneResponse>> list() {
        return ApiResponse.of(zones.findByActiveTrueAndDeletedAtIsNullOrderByName().stream()
                .map(zone -> new ZoneResponse(zone.getId(), zone.getCode(), zone.getName(), zone.getLocality()))
                .toList());
    }

    public record ZoneResponse(UUID id, String code, String name, String locality) {}
}
