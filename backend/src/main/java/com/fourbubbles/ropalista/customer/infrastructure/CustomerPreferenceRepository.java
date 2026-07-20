package com.fourbubbles.ropalista.customer.infrastructure;

import com.fourbubbles.ropalista.customer.domain.CustomerPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerPreferenceRepository extends JpaRepository<CustomerPreference, UUID> {
    Optional<CustomerPreference> findByCustomerIdAndDeletedAtIsNull(UUID customerId);
}
