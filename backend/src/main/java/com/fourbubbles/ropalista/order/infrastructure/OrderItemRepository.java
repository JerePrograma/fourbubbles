package com.fourbubbles.ropalista.order.infrastructure;

import com.fourbubbles.ropalista.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderIdAndDeletedAtIsNullOrderByCreatedAt(UUID orderId);
    void deleteByOrderId(UUID orderId);
}
