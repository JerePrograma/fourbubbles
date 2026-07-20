package ar.com.ropalista.reception.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReceptionDifferencePolicyTest {
    private final ReceptionDifferencePolicy policy = new ReceptionDifferencePolicy();

    @Test
    void acceptsSmallWeightVariationWithoutDamageOrPieceDifference() {
        var result = policy.evaluate(10, 10, 2500, 2700, false);

        assertThat(result.requiresCustomerApproval()).isFalse();
        assertThat(result.significantWeightDifference()).isFalse();
        assertThat(result.weightDifferenceGrams()).isEqualTo(200);
    }

    @Test
    void requiresApprovalWhenRelativeOrAbsoluteWeightToleranceIsExceeded() {
        var result = policy.evaluate(10, 10, 2500, 2800, false);

        assertThat(result.requiresCustomerApproval()).isTrue();
        assertThat(result.significantWeightDifference()).isTrue();
    }

    @Test
    void requiresApprovalForPieceDifferenceOrDamage() {
        assertThat(policy.evaluate(10, 9, null, 2000, false).requiresCustomerApproval()).isTrue();
        assertThat(policy.evaluate(10, 10, null, 2000, true).requiresCustomerApproval()).isTrue();
    }
}
