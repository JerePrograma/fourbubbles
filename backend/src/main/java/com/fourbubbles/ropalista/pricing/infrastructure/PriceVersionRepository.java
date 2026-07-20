package com.fourbubbles.ropalista.pricing.infrastructure;

import com.fourbubbles.ropalista.pricing.domain.PriceVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PriceVersionRepository extends JpaRepository<PriceVersion, UUID> {
    @Query("""
        select p from PriceVersion p
        where p.servicePlan.id = :planId
          and p.deletedAt is null
          and p.validFrom <= :date
          and (p.validTo is null or p.validTo >= :date)
          and (p.zone is null or p.zone.id = :zoneId)
        order by case when p.zone is null then 1 else 0 end, p.validFrom desc, p.createdAt desc
        """)
    List<PriceVersion> findApplicable(UUID planId, UUID zoneId, LocalDate date);
}
