package com.fourbubbles.ropalista.order.application;

import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import com.fourbubbles.ropalista.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStateMachineTest {
    private final OrderStateMachine stateMachine = new OrderStateMachine();

    @Test
    void allowsOperationalTransition() {
        assertThatCode(() -> stateMachine.validate(OrderStatus.WASHING, OrderStatus.DRYING))
                .doesNotThrowAnyException();
    }

    @Test
    void preventsSkippingTraceableSteps() {
        assertThatThrownBy(() -> stateMachine.validate(OrderStatus.INQUIRY, OrderStatus.CLOSED))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void allowsRewashToReturnToQueue() {
        assertThatCode(() -> stateMachine.validate(OrderStatus.REWASH_REQUIRED, OrderStatus.AWAITING_WASH))
                .doesNotThrowAnyException();
    }
}
