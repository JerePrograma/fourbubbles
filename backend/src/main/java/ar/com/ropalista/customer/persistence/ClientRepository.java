package ar.com.ropalista.customer.persistence;

import ar.com.ropalista.customer.domain.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Page<Client> findByDeletedAtIsNullAndLastNameContainingIgnoreCase(String lastName, Pageable pageable);
    Optional<Client> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByWhatsappAndDeletedAtIsNull(String whatsapp);
    boolean existsByWhatsappAndDeletedAtIsNullAndIdNot(String whatsapp, UUID id);
}
