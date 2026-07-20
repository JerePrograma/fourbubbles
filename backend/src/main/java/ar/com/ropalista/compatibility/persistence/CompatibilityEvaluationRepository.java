package ar.com.ropalista.compatibility.persistence;

import ar.com.ropalista.compatibility.domain.CompatibilityEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompatibilityEvaluationRepository extends JpaRepository<CompatibilityEvaluation, UUID> {
}
