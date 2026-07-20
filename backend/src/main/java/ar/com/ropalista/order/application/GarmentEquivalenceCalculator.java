package ar.com.ropalista.order.application;

import ar.com.ropalista.catalog.domain.GarmentEquivalence;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GarmentEquivalenceCalculator {
    public Calculation calculate(GarmentEquivalence rule, int physicalPieces) {
        if (physicalPieces <= 0) {
            throw new IllegalArgumentException("physicalPieces debe ser mayor a cero");
        }
        int groups = Math.ceilDiv(physicalPieces, rule.getPhysicalUnitsPerGroup());
        BigDecimal equivalentUnits = rule.getEquivalentUnits().multiply(BigDecimal.valueOf(groups));
        Integer estimatedWeight = rule.getEstimatedWeightGrams() == null
                ? null : Math.multiplyExact(rule.getEstimatedWeightGrams(), groups);
        return new Calculation(physicalPieces, groups, equivalentUnits, estimatedWeight,
                rule.isExclusiveCycleRequired(), rule.isQuoteRequired());
    }

    public record Calculation(int physicalPieces, int groups, BigDecimal equivalentUnits,
                              Integer estimatedWeightGrams, boolean exclusiveCycleRequired,
                              boolean quoteRequired) {}
}
