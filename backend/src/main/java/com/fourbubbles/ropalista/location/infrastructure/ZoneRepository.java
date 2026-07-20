package com.fourbubbles.ropalista.location.infrastructure;

import com.fourbubbles.ropalista.location.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ZoneRepository extends JpaRepository<Zone, UUID> {
    List<Zone> findByActiveTrueAndDeletedAtIsNullOrderByName();
    boolean existsByIdAndActiveTrueAndDeletedAtIsNull(UUID id);
}
