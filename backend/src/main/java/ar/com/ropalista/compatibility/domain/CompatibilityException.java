package ar.com.ropalista.compatibility.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "compatibility_exceptions")
public class CompatibilityException extends AuditableEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false, unique = true)
    private CompatibilityEvaluation evaluation;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(name = "authorized_by", nullable = false, length = 100)
    private String authorizedBy;

    @Column(name = "authorized_at", nullable = false)
    private OffsetDateTime authorizedAt;

    public CompatibilityException(String reason, String authorizedBy, OffsetDateTime authorizedAt) {
        this.reason = reason;
        this.authorizedBy = authorizedBy;
        this.authorizedAt = authorizedAt;
    }

    void attach(CompatibilityEvaluation evaluation) {
        this.evaluation = evaluation;
    }
}
