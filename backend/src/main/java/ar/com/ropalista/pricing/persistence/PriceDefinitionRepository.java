package ar.com.ropalista.pricing.persistence;

import ar.com.ropalista.pricing.domain.PriceDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PriceDefinitionRepository extends JpaRepository<PriceDefinition, UUID> {
    @Query("""
            select p from PriceDefinition p
            where p.service.id = :serviceId
              and p.active = true
              and p.validFrom <= :at
              and (p.validTo is null or p.validTo >= :at)
              and (p.zone is null or p.zone.id = :zoneId)
            order by case when p.zone is null then 1 else 0 end, p.validFrom desc
            """)
    List<PriceDefinition> findApplicable(@Param("serviceId") UUID serviceId,
                                         @Param("zoneId") UUID zoneId,
                                         @Param("at") OffsetDateTime at);
}
