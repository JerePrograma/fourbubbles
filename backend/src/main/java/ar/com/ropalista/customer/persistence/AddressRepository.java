package ar.com.ropalista.customer.persistence;

import ar.com.ropalista.customer.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    Optional<Address> findByIdAndClientIdAndActiveTrue(UUID id, UUID clientId);
    List<Address> findByClientIdAndActiveTrueOrderByPrimaryAddressDescValidFromDesc(UUID clientId);
    List<Address> findByClientIdAndActiveFalseOrderByValidToDesc(UUID clientId);
    long countByClientIdAndActiveTrue(UUID clientId);
}
