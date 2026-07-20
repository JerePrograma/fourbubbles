package com.fourbubbles.ropalista.customer.infrastructure;

import com.fourbubbles.ropalista.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    List<Customer> findByDeletedAtIsNullOrderByLastNameAscFirstNameAsc();
    long countByIdAndDeletedAtIsNull(UUID id);
}
