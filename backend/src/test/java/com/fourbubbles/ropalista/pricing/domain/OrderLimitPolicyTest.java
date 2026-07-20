package com.fourbubbles.ropalista.pricing.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderLimitPolicyTest {
    private final OrderLimitPolicy policy = new OrderLimitPolicy();

    @Test
    void equivalentLimitIsReachedFirst() {
        var result = policy.evaluate(new BigDecimal("12"), 2000, new BigDecimal("12"), 2500);
        assertThat(result.exceeded()).isFalse();
        assertThat(result.firstLimit()).isEqualTo(OrderLimitPolicy.LimitFactor.EQUIVALENT_UNITS);
    }

    @Test
    void weightLimitIsReachedFirst() {
        var result = policy.evaluate(new BigDecimal("10"), 2500, new BigDecimal("12"), 2500);
        assertThat(result.exceeded()).isFalse();
        assertThat(result.firstLimit()).isEqualTo(OrderLimitPolicy.LimitFactor.WEIGHT);
    }

    @Test
    void rejectsMoreThanTwelveUnits() {
        var result = policy.evaluate(new BigDecimal("12.01"), 2000, new BigDecimal("12"), 2500);
        assertThat(result.exceeded()).isTrue();
        assertThat(result.equivalentUnitsExceeded()).isTrue();
    }

    @Test
    void rejectsMoreThanTwoAndHalfKilograms() {
        var result = policy.evaluate(new BigDecimal("10"), 2501, new BigDecimal("12"), 2500);
        assertThat(result.exceeded()).isTrue();
        assertThat(result.weightExceeded()).isTrue();
    }
}
