package ar.com.ropalista.production.persistence;

import ar.com.ropalista.production.domain.ProductionProgram;
import ar.com.ropalista.production.domain.ProductionStage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionProgramRepository extends JpaRepository<ProductionProgram, UUID> {
    Optional<ProductionProgram> findByCodeIgnoreCase(String code);

    List<ProductionProgram> findAllByOrderByStageAscNameAsc();

    List<ProductionProgram> findAllByStageAndActiveTrueOrderByNameAsc(ProductionStage stage);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductionProgram p where p.id = :id")
    Optional<ProductionProgram> findByIdForUpdate(@Param("id") UUID id);
}
