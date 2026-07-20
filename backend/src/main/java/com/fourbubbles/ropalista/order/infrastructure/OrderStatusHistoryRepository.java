package com.fourbubbles.ropalista.order.infrastructure;

import com.fourbubbles.ropalista.order.domain.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {
    List<OrderStatusHistory> findByOrderIdAndDeletedAtIsNullOrderByCreatedAt(UUID orderId);
}
