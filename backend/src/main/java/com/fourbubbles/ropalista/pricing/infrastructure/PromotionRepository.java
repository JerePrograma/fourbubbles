package com.fourbubbles.ropalista.pricing.infrastructure;

import com.fourbubbles.ropalista.pricing.domain.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    Optional<Promotion> findByCodeIgnoreCaseAndActiveTrueAndDeletedAtIsNull(String code);
}
