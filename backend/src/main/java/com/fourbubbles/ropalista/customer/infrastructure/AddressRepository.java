package com.fourbubbles.ropalista.customer.infrastructure;

import com.fourbubbles.ropalista.customer.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByCustomerIdAndDeletedAtIsNullOrderByPrimaryDesc(UUID customerId);
    Optional<Address> findByIdAndCustomerIdAndDeletedAtIsNull(UUID id, UUID customerId);
}
