package ar.com.ropalista.order.application;

import ar.com.ropalista.catalog.domain.GarmentEquivalence;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GarmentEquivalenceCalculatorTest {
    private final GarmentEquivalenceCalculator calculator = new GarmentEquivalenceCalculator();

    @Test
    void groupsSixPairsOfSocksWithoutLosingPhysicalPieces() {
        GarmentEquivalence socks = rule(6, "1.00", 180);

        var result = calculator.calculate(socks, 12);

        assertThat(result.physicalPieces()).isEqualTo(12);
        assertThat(result.groups()).isEqualTo(2);
        assertThat(result.equivalentUnits()).isEqualByComparingTo("2.00");
        assertThat(result.estimatedWeightGrams()).isEqualTo(360);
    }

    @Test
    void roundsIncompleteUnderwearGroupUp() {
        GarmentEquivalence underwear = rule(3, "1.00", 180);

        var result = calculator.calculate(underwear, 4);

        assertThat(result.groups()).isEqualTo(2);
        assertThat(result.equivalentUnits()).isEqualByComparingTo("2.00");
    }

    @Test
    void rejectsNonPositivePhysicalPieceCount() {
        assertThatThrownBy(() -> calculator.calculate(rule(1, "1", 100), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private GarmentEquivalence rule(int groupSize, String units, Integer weight) {
        GarmentEquivalence rule = mock(GarmentEquivalence.class);
        when(rule.getPhysicalUnitsPerGroup()).thenReturn(groupSize);
        when(rule.getEquivalentUnits()).thenReturn(new BigDecimal(units));
        when(rule.getEstimatedWeightGrams()).thenReturn(weight);
        return rule;
    }
}
