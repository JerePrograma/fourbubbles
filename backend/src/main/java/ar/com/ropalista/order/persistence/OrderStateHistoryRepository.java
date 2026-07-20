package ar.com.ropalista.order.persistence;

import ar.com.ropalista.order.domain.OrderStateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderStateHistoryRepository extends JpaRepository<OrderStateHistory, UUID> {}
