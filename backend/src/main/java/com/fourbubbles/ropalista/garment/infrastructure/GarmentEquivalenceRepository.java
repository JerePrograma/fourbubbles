package com.fourbubbles.ropalista.garment.infrastructure;

import com.fourbubbles.ropalista.garment.domain.GarmentEquivalence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GarmentEquivalenceRepository extends JpaRepository<GarmentEquivalence, UUID> {
    List<GarmentEquivalence> findByActiveTrueAndDeletedAtIsNullOrderByCategoryAscNameAsc();
    Optional<GarmentEquivalence> findByIdAndActiveTrueAndDeletedAtIsNull(UUID id);
}
