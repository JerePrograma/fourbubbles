package ar.com.ropalista.production.persistence;

import ar.com.ropalista.production.domain.ProductionCycle;
import ar.com.ropalista.production.domain.ProductionCycleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductionCycleRepository extends JpaRepository<ProductionCycle, UUID>,
        JpaSpecificationExecutor<ProductionCycle> {

    @EntityGraph(attributePaths = {"machine", "program", "orders", "orders.order"})
    Optional<ProductionCycle> findByIdempotencyKey(String idempotencyKey);

    boolean existsByMachineIdAndStatusIn(UUID machineId, Iterable<ProductionCycleStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"machine", "program", "orders", "orders.order"})
    @Query("select c from ProductionCycle c where c.id = :id")
    Optional<ProductionCycle> findByIdForUpdate(@Param("id") UUID id);

    @EntityGraph(attributePaths = {"machine", "program", "orders", "orders.order"})
    @Query("select c from ProductionCycle c where c.id = :id")
    Optional<ProductionCycle> findDetailedById(@Param("id") UUID id);
}
