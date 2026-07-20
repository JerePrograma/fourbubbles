package ar.com.ropalista.catalog.persistence;

import ar.com.ropalista.catalog.domain.GarmentEquivalence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GarmentEquivalenceRepository extends JpaRepository<GarmentEquivalence, UUID> {
    @Query("""
            select e from GarmentEquivalence e
            where lower(e.code) = lower(:code)
              and e.active = true
              and e.validFrom <= :date
              and (e.validTo is null or e.validTo >= :date)
            order by e.validFrom desc
            """)
    List<GarmentEquivalence> findApplicable(@Param("code") String code, @Param("date") LocalDate date);

    @Query("""
            select e from GarmentEquivalence e
            where e.active = true
              and e.validFrom <= :date
              and (e.validTo is null or e.validTo >= :date)
            order by e.category, e.name
            """)
    List<GarmentEquivalence> findAllApplicable(@Param("date") LocalDate date);

    default Optional<GarmentEquivalence> findActive(String code, LocalDate date) {
        return findApplicable(code, date).stream().findFirst();
    }
}
