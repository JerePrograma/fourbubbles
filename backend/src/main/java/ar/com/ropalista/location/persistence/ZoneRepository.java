package ar.com.ropalista.location.persistence;

import ar.com.ropalista.location.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {
    Optional<Zone> findByCodeAndActiveTrue(String code);
}
