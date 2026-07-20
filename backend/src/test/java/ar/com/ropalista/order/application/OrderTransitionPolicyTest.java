package ar.com.ropalista.order.application;

import ar.com.ropalista.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTransitionPolicyTest {
    private final OrderTransitionPolicy policy = new OrderTransitionPolicy();

    @Test
    void allowsExpectedOperationalProgression() {
        assertThat(policy.canTransition(OrderStatus.WAITING_WASH, OrderStatus.WASHING)).isTrue();
        assertThat(policy.canTransition(OrderStatus.WASHING, OrderStatus.QUALITY_CONTROL)).isTrue();
        assertThat(policy.canTransition(OrderStatus.QUALITY_CONTROL, OrderStatus.REWASH_REQUIRED)).isTrue();
    }

    @Test
    void preventsSkippingTraceableSteps() {
        assertThat(policy.canTransition(OrderStatus.RECEIVED, OrderStatus.DELIVERED)).isFalse();
        assertThat(policy.canTransition(OrderStatus.CLOSED, OrderStatus.WASHING)).isFalse();
    }
}
