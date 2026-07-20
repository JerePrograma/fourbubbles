package ar.com.ropalista.order.application;

import ar.com.ropalista.order.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class OrderTransitionPolicy {
    private final Map<OrderStatus, Set<OrderStatus>> allowed = new EnumMap<>(OrderStatus.class);

    public OrderTransitionPolicy() {
        allow(OrderStatus.INQUIRY, OrderStatus.QUOTED, OrderStatus.CANCELLED);
        allow(OrderStatus.QUOTED, OrderStatus.WAITING_CONFIRMATION, OrderStatus.RESERVED, OrderStatus.CANCELLED);
        allow(OrderStatus.WAITING_CONFIRMATION, OrderStatus.RESERVED, OrderStatus.CANCELLED);
        allow(OrderStatus.RESERVED, OrderStatus.PICKUP_SCHEDULED, OrderStatus.CANCELLED);
        allow(OrderStatus.PICKUP_SCHEDULED, OrderStatus.PICKED_UP, OrderStatus.CANCELLED);
        allow(OrderStatus.PICKED_UP, OrderStatus.RECEIVED);
        allow(OrderStatus.RECEIVED, OrderStatus.PENDING_INSPECTION);
        allow(OrderStatus.PENDING_INSPECTION, OrderStatus.WAITING_PRICE_APPROVAL, OrderStatus.CLASSIFIED);
        allow(OrderStatus.WAITING_PRICE_APPROVAL, OrderStatus.CLASSIFIED, OrderStatus.CANCELLED);
        allow(OrderStatus.CLASSIFIED, OrderStatus.WAITING_WASH);
        allow(OrderStatus.WAITING_WASH, OrderStatus.WASHING);
        allow(OrderStatus.WASHING, OrderStatus.DRYING, OrderStatus.QUALITY_CONTROL);
        allow(OrderStatus.DRYING, OrderStatus.QUALITY_CONTROL);
        allow(OrderStatus.QUALITY_CONTROL, OrderStatus.REWASH_REQUIRED, OrderStatus.FOLDING);
        allow(OrderStatus.REWASH_REQUIRED, OrderStatus.WAITING_WASH);
        allow(OrderStatus.FOLDING, OrderStatus.PACKAGED);
        allow(OrderStatus.PACKAGED, OrderStatus.READY_FOR_DELIVERY);
        allow(OrderStatus.READY_FOR_DELIVERY, OrderStatus.PAYMENT_PENDING, OrderStatus.DELIVERY_SCHEDULED);
        allow(OrderStatus.PAYMENT_PENDING, OrderStatus.DELIVERY_SCHEDULED);
        allow(OrderStatus.DELIVERY_SCHEDULED, OrderStatus.DELIVERED);
        allow(OrderStatus.DELIVERED, OrderStatus.CLOSED, OrderStatus.CLAIM);
        allow(OrderStatus.CLOSED, OrderStatus.CLAIM);
        allow(OrderStatus.CLAIM, OrderStatus.CLOSED, OrderStatus.PARTIALLY_REFUNDED, OrderStatus.FULLY_REFUNDED);
    }

    private void allow(OrderStatus from, OrderStatus... to) {
        allowed.put(from, EnumSet.copyOf(java.util.List.of(to)));
    }

    public boolean canTransition(OrderStatus from, OrderStatus to) {
        return allowedTransitions(from).contains(to);
    }

    public Set<OrderStatus> allowedTransitions(OrderStatus from) {
        Set<OrderStatus> transitions = allowed.get(from);
        return transitions == null ? Set.of() : Set.copyOf(transitions);
    }
}
