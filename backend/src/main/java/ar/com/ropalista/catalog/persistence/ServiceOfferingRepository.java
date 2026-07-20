package ar.com.ropalista.catalog.persistence;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, UUID> {
    @Query("""
            select s from ServiceOffering s
            where lower(s.code) = lower(:code)
              and s.active = true
              and s.validFrom <= :date
              and (s.validTo is null or s.validTo >= :date)
            order by s.validFrom desc
            """)
    List<ServiceOffering> findApplicable(@Param("code") String code, @Param("date") LocalDate date);

    default Optional<ServiceOffering> findActive(String code, LocalDate date) {
        return findApplicable(code, date).stream().findFirst();
    }
}
