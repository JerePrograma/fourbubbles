package com.fourbubbles.ropalista.pricing.infrastructure;

import com.fourbubbles.ropalista.pricing.domain.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {
    boolean existsByPromotionIdAndAddressIdAndDeletedAtIsNull(UUID promotionId, UUID addressId);
    long countByPromotionIdAndDeletedAtIsNull(UUID promotionId);
    long countByPromotionIdAndCreatedAtBetweenAndDeletedAtIsNull(UUID promotionId, Instant from, Instant to);
}
