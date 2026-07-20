package ar.com.ropalista.reception.persistence;

import ar.com.ropalista.reception.domain.OrderReception;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderReceptionRepository extends JpaRepository<OrderReception, UUID> {
    Optional<OrderReception> findByOrderId(UUID orderId);
    Optional<OrderReception> findByIdempotencyKey(String idempotencyKey);
}
