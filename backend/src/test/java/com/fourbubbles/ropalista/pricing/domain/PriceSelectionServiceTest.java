package com.fourbubbles.ropalista.pricing.domain;

import com.fourbubbles.ropalista.location.domain.Zone;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PriceSelectionServiceTest {
    private final PriceSelectionService service = new PriceSelectionService();

    @Test
    void selectsZoneSpecificActivePriceOverGenericPrice() {
        UUID zoneId = UUID.randomUUID();
        Zone zone = new Zone();
        zone.setId(zoneId);

        PriceVersion generic = price("6500", LocalDate.of(2026, 1, 1), null, null);
        PriceVersion specific = price("6200", LocalDate.of(2026, 7, 1), null, zone);

        PriceVersion selected = service.select(List.of(generic, specific), zoneId, LocalDate.of(2026, 7, 20));
        assertThat(selected.getAmount()).isEqualByComparingTo("6200");
    }

    @Test
    void ignoresExpiredPrices() {
        UUID zoneId = UUID.randomUUID();
        PriceVersion expired = price("5000", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), null);
        PriceVersion current = price("6500", LocalDate.of(2026, 1, 1), null, null);

        PriceVersion selected = service.select(List.of(expired, current), zoneId, LocalDate.of(2026, 7, 20));
        assertThat(selected.getAmount()).isEqualByComparingTo("6500");
    }

    private PriceVersion price(String amount, LocalDate from, LocalDate to, Zone zone) {
        PriceVersion price = new PriceVersion();
        price.setAmount(new BigDecimal(amount));
        price.setCurrency("ARS");
        price.setValidFrom(from);
        price.setValidTo(to);
        price.setZone(zone);
        return price;
    }
}
