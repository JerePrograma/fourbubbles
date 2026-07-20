package ar.com.ropalista.order.persistence;

import ar.com.ropalista.order.domain.LaundryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LaundryOrderRepository extends JpaRepository<LaundryOrder, UUID> {
    Optional<LaundryOrder> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByClientIdAndDeletedAtIsNull(UUID clientId);
}
