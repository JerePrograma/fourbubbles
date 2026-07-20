package ar.com.ropalista.order.application;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.common.api.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderLimitPolicy {
    public LimitResult evaluate(ServiceOffering service, BigDecimal equivalentUnits, Integer weightGrams) {
        boolean equivalentExceeded = service.getMaxEquivalentUnits() != null
                && equivalentUnits.compareTo(service.getMaxEquivalentUnits()) > 0;
        boolean weightExceeded = weightGrams != null && service.getMaxWeightGrams() != null
                && weightGrams > service.getMaxWeightGrams();
        boolean safeCapacityExceeded = weightGrams != null && service.getSafeCapacityGrams() != null
                && weightGrams > service.getSafeCapacityGrams();

        if (safeCapacityExceeded) {
            throw new BusinessException("SAFE_CAPACITY_EXCEEDED", "El pedido supera la capacidad segura configurada", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        String firstLimit = equivalentExceeded && weightExceeded ? "BOTH" : equivalentExceeded ? "EQUIVALENT_UNITS" : weightExceeded ? "WEIGHT" : "NONE";
        return new LimitResult(equivalentExceeded, weightExceeded, firstLimit,
                equivalentExceeded || weightExceeded);
    }

    public record LimitResult(boolean equivalentExceeded, boolean weightExceeded,
                              String firstLimitReached, boolean splitOrDifferentServiceRequired) {}
}
