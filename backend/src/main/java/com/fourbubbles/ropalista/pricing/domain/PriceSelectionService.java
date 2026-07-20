package com.fourbubbles.ropalista.pricing.domain;

import com.fourbubbles.ropalista.common.application.BusinessRuleException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PriceSelectionService {
    public PriceVersion select(List<PriceVersion> candidates, UUID zoneId, LocalDate date) {
        return candidates.stream()
                .filter(price -> price.isValidOn(date))
                .filter(price -> price.getZone() == null || price.getZone().getId().equals(zoneId))
                .sorted(Comparator
                        .comparing((PriceVersion price) -> price.getZone() != null).reversed()
                        .thenComparing(PriceVersion::getValidFrom).reversed())
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("PRICE_NOT_FOUND",
                        "No existe un precio vigente para el servicio y la zona"));
    }
}
