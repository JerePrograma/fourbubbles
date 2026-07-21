package ar.com.ropalista.production.persistence;

import ar.com.ropalista.production.domain.ProductionProgram;
import ar.com.ropalista.production.domain.ProductionStage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductionProgramRepository extends JpaRepository<ProductionProgram, UUID> {
    Optional<ProductionProgram> findByCodeIgnoreCase(String code);

    List<ProductionProgram> findAllByOrderByStageAscNameAsc();

    List<ProductionProgram> findAllByStageAndActiveTrueOrderByNameAsc(ProductionStage stage);
}
