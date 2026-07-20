package com.fourbubbles.ropalista.pricing.infrastructure;

import com.fourbubbles.ropalista.pricing.domain.ServicePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServicePlanRepository extends JpaRepository<ServicePlan, UUID> {
    List<ServicePlan> findByActiveTrueAndDeletedAtIsNullOrderByName();
    Optional<ServicePlan> findByIdAndActiveTrueAndDeletedAtIsNull(UUID id);
}
