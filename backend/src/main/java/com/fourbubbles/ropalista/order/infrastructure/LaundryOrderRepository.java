package com.fourbubbles.ropalista.order.infrastructure;

import com.fourbubbles.ropalista.order.domain.LaundryOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LaundryOrderRepository extends JpaRepository<LaundryOrder, UUID> {
    List<LaundryOrder> findByDeletedAtIsNullOrderByCreatedAtDesc();
    Optional<LaundryOrder> findByIdAndDeletedAtIsNull(UUID id);
    long countByCustomerIdAndDeletedAtIsNull(UUID customerId);
}
