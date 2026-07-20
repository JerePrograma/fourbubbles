package ar.com.ropalista.payment.persistence;

import ar.com.ropalista.payment.domain.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {
    Optional<PaymentMethod> findByCodeAndActiveTrue(String code);
}
