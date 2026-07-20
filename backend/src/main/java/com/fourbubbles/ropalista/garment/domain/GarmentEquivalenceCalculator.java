package com.fourbubbles.ropalista.garment.domain;

import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GarmentEquivalenceCalculator {
    public EquivalenceResult calculate(int physicalPieces, int physicalUnitsPerGroup, BigDecimal unitsPerGroup) {
        if (physicalPieces <= 0) {
            throw new BusinessRuleException("INVALID_PHYSICAL_COUNT", "La cantidad física debe ser mayor a cero");
        }
        if (physicalUnitsPerGroup <= 0 || unitsPerGroup == null || unitsPerGroup.signum() <= 0) {
            throw new BusinessRuleException("INVALID_EQUIVALENCE_RULE", "La equivalencia configurada no es válida");
        }
        int groups = Math.ceilDiv(physicalPieces, physicalUnitsPerGroup);
        return new EquivalenceResult(physicalPieces, groups, unitsPerGroup.multiply(BigDecimal.valueOf(groups)));
    }
}
