package ar.com.ropalista.pricing.persistence;

import ar.com.ropalista.pricing.domain.PromotionUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, UUID> {
    boolean existsByPromotionIdAndAddressId(UUID promotionId, UUID addressId);
    boolean existsByOrderId(UUID orderId);
    long countByPromotionId(UUID promotionId);

    @Query("select count(u) from PromotionUsage u where u.promotion.id = :promotionId and u.createdAt >= :from and u.createdAt < :to")
    long countInPeriod(@Param("promotionId") UUID promotionId,
                       @Param("from") OffsetDateTime from,
                       @Param("to") OffsetDateTime to);
}
