package com.fourbubbles.ropalista.garment.domain;

import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarmentEquivalenceCalculatorTest {
    private final GarmentEquivalenceCalculator calculator = new GarmentEquivalenceCalculator();

    @Test
    void calculatesSinglePhysicalItem() {
        EquivalenceResult result = calculator.calculate(2, 1, new BigDecimal("1.00"));
        assertThat(result.groups()).isEqualTo(2);
        assertThat(result.equivalentUnits()).isEqualByComparingTo("2.00");
    }

    @Test
    void groupsSixPairsOfSocksAsTwoCommercialUnits() {
        EquivalenceResult result = calculator.calculate(12, 6, BigDecimal.ONE);
        assertThat(result.physicalPieces()).isEqualTo(12);
        assertThat(result.groups()).isEqualTo(2);
        assertThat(result.equivalentUnits()).isEqualByComparingTo("2");
    }

    @Test
    void groupsSixUnderwearItemsAsTwoCommercialUnits() {
        EquivalenceResult result = calculator.calculate(6, 3, BigDecimal.ONE);
        assertThat(result.groups()).isEqualTo(2);
        assertThat(result.equivalentUnits()).isEqualByComparingTo("2");
    }

    @Test
    void incompleteGroupRoundsUpBecauseItConsumesCommercialCapacity() {
        EquivalenceResult result = calculator.calculate(7, 6, BigDecimal.ONE);
        assertThat(result.groups()).isEqualTo(2);
    }

    @Test
    void rejectsInvalidPhysicalCount() {
        assertThatThrownBy(() -> calculator.calculate(0, 1, BigDecimal.ONE))
                .isInstanceOf(BusinessRuleException.class);
    }
}
