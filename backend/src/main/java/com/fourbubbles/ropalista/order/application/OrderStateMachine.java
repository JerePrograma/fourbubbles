package com.fourbubbles.ropalista.order.application;

import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import com.fourbubbles.ropalista.order.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderStateMachine {
    private final Map<OrderStatus, Set<OrderStatus>> allowed = new EnumMap<>(OrderStatus.class);

    public OrderStateMachine() {
        allow(OrderStatus.INQUIRY, OrderStatus.QUOTED, OrderStatus.CANCELLED);
        allow(OrderStatus.QUOTED, OrderStatus.AWAITING_CONFIRMATION, OrderStatus.RESERVED, OrderStatus.CANCELLED);
        allow(OrderStatus.AWAITING_CONFIRMATION, OrderStatus.RESERVED, OrderStatus.CANCELLED);
        allow(OrderStatus.RESERVED, OrderStatus.PICKUP_SCHEDULED, OrderStatus.CANCELLED);
        allow(OrderStatus.PICKUP_SCHEDULED, OrderStatus.PICKED_UP, OrderStatus.CANCELLED);
        allow(OrderStatus.PICKED_UP, OrderStatus.RECEIVED);
        allow(OrderStatus.RECEIVED, OrderStatus.PENDING_INSPECTION, OrderStatus.CLASSIFIED);
        allow(OrderStatus.PENDING_INSPECTION, OrderStatus.AWAITING_PRICE_APPROVAL, OrderStatus.CLASSIFIED);
        allow(OrderStatus.AWAITING_PRICE_APPROVAL, OrderStatus.CLASSIFIED, OrderStatus.CANCELLED);
        allow(OrderStatus.CLASSIFIED, OrderStatus.AWAITING_WASH);
        allow(OrderStatus.AWAITING_WASH, OrderStatus.WASHING);
        allow(OrderStatus.WASHING, OrderStatus.DRYING, OrderStatus.REWASH_REQUIRED);
        allow(OrderStatus.DRYING, OrderStatus.QUALITY_CONTROL);
        allow(OrderStatus.QUALITY_CONTROL, OrderStatus.REWASH_REQUIRED, OrderStatus.FOLDING);
        allow(OrderStatus.REWASH_REQUIRED, OrderStatus.AWAITING_WASH);
        allow(OrderStatus.FOLDING, OrderStatus.BAGGED);
        allow(OrderStatus.BAGGED, OrderStatus.READY_FOR_DELIVERY);
        allow(OrderStatus.READY_FOR_DELIVERY, OrderStatus.PAYMENT_PENDING, OrderStatus.DELIVERY_SCHEDULED);
        allow(OrderStatus.PAYMENT_PENDING, OrderStatus.DELIVERY_SCHEDULED);
        allow(OrderStatus.DELIVERY_SCHEDULED, OrderStatus.DELIVERED);
        allow(OrderStatus.DELIVERED, OrderStatus.CLOSED, OrderStatus.CLAIM);
        allow(OrderStatus.CLOSED, OrderStatus.CLAIM);
        allow(OrderStatus.CLAIM, OrderStatus.PARTIALLY_REFUNDED, OrderStatus.FULLY_REFUNDED, OrderStatus.CLOSED);
    }

    public void validate(OrderStatus current, OrderStatus target) {
        if (!allowed.getOrDefault(current, Set.of()).contains(target)) {
            throw new BusinessRuleException("INVALID_ORDER_TRANSITION",
                    "No se permite cambiar el pedido de " + current + " a " + target);
        }
    }

    private void allow(OrderStatus from, OrderStatus... to) {
        allowed.put(from, EnumSet.of(to[0], to));
    }
}
