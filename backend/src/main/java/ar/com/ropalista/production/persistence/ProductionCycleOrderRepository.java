package ar.com.ropalista.production.persistence;

import ar.com.ropalista.production.domain.ProductionCycleOrder;
import ar.com.ropalista.production.domain.ProductionCycleStatus;
import ar.com.ropalista.production.domain.ProductionStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;

public interface ProductionCycleOrderRepository extends JpaRepository<ProductionCycleOrder, UUID> {
    @Query("""
            select (count(a) > 0) from ProductionCycleOrder a
            where a.order.id = :orderId
              and a.cycle.status in :statuses
              and a.cycle.program.stage = :stage
            """)
    boolean existsActiveAssignment(@Param("orderId") UUID orderId,
                                   @Param("stage") ProductionStage stage,
                                   @Param("statuses") Collection<ProductionCycleStatus> statuses);
}
