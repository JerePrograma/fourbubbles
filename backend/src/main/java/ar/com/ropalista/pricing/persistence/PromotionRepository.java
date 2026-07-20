package ar.com.ropalista.pricing.persistence;

import ar.com.ropalista.pricing.domain.Promotion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    @Query("""
            select p from Promotion p
            where lower(p.code) = lower(:code)
              and p.status = 'ACTIVE'
              and p.validFrom <= :at
              and (p.validTo is null or p.validTo >= :at)
            order by p.validFrom desc
            """)
    List<Promotion> findApplicable(@Param("code") String code, @Param("at") OffsetDateTime at);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Promotion p where p.id = :id")
    Optional<Promotion> findByIdForUpdate(@Param("id") UUID id);

    default Optional<Promotion> findActive(String code, OffsetDateTime at) {
        return findApplicable(code, at).stream().findFirst();
    }
}
