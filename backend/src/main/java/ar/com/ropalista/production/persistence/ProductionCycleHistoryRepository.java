package ar.com.ropalista.production.persistence;

import ar.com.ropalista.production.domain.ProductionCycleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductionCycleHistoryRepository extends JpaRepository<ProductionCycleHistory, UUID> {
    List<ProductionCycleHistory> findAllByCycleIdOrderByCreatedAtAsc(UUID cycleId);
}
