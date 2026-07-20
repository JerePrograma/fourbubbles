package com.fourbubbles.ropalista.pricing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OrderLimitPolicy {
    public LimitResult evaluate(BigDecimal equivalentUnits, int weightGrams,
                                BigDecimal maxEquivalentUnits, int maxWeightGrams) {
        boolean equivalentExceeded = equivalentUnits.compareTo(maxEquivalentUnits) > 0;
        boolean weightExceeded = weightGrams > maxWeightGrams;
        BigDecimal equivalentRatio = equivalentUnits.divide(maxEquivalentUnits, 6, RoundingMode.HALF_UP);
        BigDecimal weightRatio = BigDecimal.valueOf(weightGrams)
                .divide(BigDecimal.valueOf(maxWeightGrams), 6, RoundingMode.HALF_UP);

        LimitFactor factor;
        if (equivalentRatio.compareTo(weightRatio) >= 0) {
            factor = LimitFactor.EQUIVALENT_UNITS;
        } else {
            factor = LimitFactor.WEIGHT;
        }
        return new LimitResult(equivalentExceeded || weightExceeded, equivalentExceeded, weightExceeded, factor);
    }

    public enum LimitFactor { EQUIVALENT_UNITS, WEIGHT }

    public record LimitResult(boolean exceeded, boolean equivalentUnitsExceeded,
                              boolean weightExceeded, LimitFactor firstLimit) {}
}
