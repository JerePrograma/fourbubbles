package ar.com.ropalista.reception.persistence;

import ar.com.ropalista.reception.domain.OrderReception;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderReceptionRepository extends JpaRepository<OrderReception, UUID> {
    @EntityGraph(attributePaths = {"order", "items", "items.equivalence", "evidences"})
    Optional<OrderReception> findByOrderId(UUID orderId);

    @EntityGraph(attributePaths = {"order", "items", "items.equivalence", "evidences"})
    Optional<OrderReception> findByIdempotencyKey(String idempotencyKey);
}
