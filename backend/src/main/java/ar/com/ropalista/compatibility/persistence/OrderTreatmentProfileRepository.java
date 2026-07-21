package ar.com.ropalista.compatibility.persistence;

import ar.com.ropalista.compatibility.domain.OrderTreatmentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderTreatmentProfileRepository extends JpaRepository<OrderTreatmentProfile, UUID> {
    Optional<OrderTreatmentProfile> findByOrderId(UUID orderId);
}
