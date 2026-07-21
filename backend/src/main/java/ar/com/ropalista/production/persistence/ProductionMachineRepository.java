package ar.com.ropalista.production.persistence;

import ar.com.ropalista.production.domain.ProductionMachine;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionMachineRepository extends JpaRepository<ProductionMachine, UUID> {
    Optional<ProductionMachine> findByCodeIgnoreCase(String code);

    List<ProductionMachine> findAllByOrderByNameAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from ProductionMachine m where m.id = :id")
    Optional<ProductionMachine> findByIdForUpdate(@Param("id") UUID id);
}
