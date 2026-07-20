package ar.com.ropalista.order.application;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.common.api.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderLimitPolicyTest {
    private final OrderLimitPolicy policy = new OrderLimitPolicy();

    @Test
    void acceptsExactConfiguredLimits() {
        var result = policy.evaluate(service("12", 2500, 2500), new BigDecimal("12"), 2500);

        assertThat(result.splitOrDifferentServiceRequired()).isFalse();
        assertThat(result.firstLimitReached()).isEqualTo("NONE");
    }

    @Test
    void detectsEquivalentLimitBeforeWeightLimit() {
        var result = policy.evaluate(service("12", 2500, 3000), new BigDecimal("13"), 2400);

        assertThat(result.equivalentExceeded()).isTrue();
        assertThat(result.weightExceeded()).isFalse();
        assertThat(result.firstLimitReached()).isEqualTo("EQUIVALENT_UNITS");
    }

    @Test
    void detectsWeightLimit() {
        var result = policy.evaluate(service("12", 2500, 3000), new BigDecimal("10"), 2600);

        assertThat(result.weightExceeded()).isTrue();
        assertThat(result.firstLimitReached()).isEqualTo("WEIGHT");
    }

    @Test
    void blocksUnsafeCapacityInsteadOfMerelyWarning() {
        assertThatThrownBy(() -> policy.evaluate(service("12", 2500, 2800), new BigDecimal("10"), 2801))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("capacidad segura");
    }

    private ServiceOffering service(String maxUnits, int maxWeight, int safeCapacity) {
        ServiceOffering service = mock(ServiceOffering.class);
        when(service.getMaxEquivalentUnits()).thenReturn(new BigDecimal(maxUnits));
        when(service.getMaxWeightGrams()).thenReturn(maxWeight);
        when(service.getSafeCapacityGrams()).thenReturn(safeCapacity);
        return service;
    }
}
