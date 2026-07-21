package ar.com.ropalista.compatibility.persistence;

import ar.com.ropalista.compatibility.domain.CompatibilityEvaluation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompatibilityEvaluationRepository extends JpaRepository<CompatibilityEvaluation, UUID>,
        JpaSpecificationExecutor<CompatibilityEvaluation> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from CompatibilityEvaluation e where e.id = :id")
    Optional<CompatibilityEvaluation> findByIdForUpdate(@Param("id") UUID id);
}
