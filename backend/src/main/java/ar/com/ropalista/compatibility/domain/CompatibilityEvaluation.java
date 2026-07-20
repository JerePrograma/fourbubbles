package ar.com.ropalista.compatibility.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.order.domain.LaundryOrder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "compatibility_evaluations")
public class CompatibilityEvaluation extends AuditableEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_a_id", nullable = false)
    private LaundryOrder orderA;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_b_id", nullable = false)
    private LaundryOrder orderB;

    @Column(name = "profile_a_version", nullable = false)
    private long profileAVersion;

    @Column(name = "profile_b_version", nullable = false)
    private long profileBVersion;

    @Column(name = "rule_version", nullable = false, length = 40)
    private String ruleVersion;

    @Column(nullable = false)
    private boolean compatible;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String reasons;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String recommendation;

    @OneToOne(mappedBy = "evaluation", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private CompatibilityException exception;

    public CompatibilityEvaluation(LaundryOrder orderA, LaundryOrder orderB,
                                   long profileAVersion, long profileBVersion,
                                   String ruleVersion, boolean compatible,
                                   String reasons, String recommendation) {
        this.orderA = orderA;
        this.orderB = orderB;
        this.profileAVersion = profileAVersion;
        this.profileBVersion = profileBVersion;
        this.ruleVersion = ruleVersion;
        this.compatible = compatible;
        this.reasons = reasons;
        this.recommendation = recommendation;
    }

    public void authorizeException(CompatibilityException exception) {
        if (compatible) {
            throw new IllegalStateException("Una evaluación compatible no requiere excepción");
        }
        if (this.exception != null) {
            throw new IllegalStateException("La evaluación ya tiene una excepción");
        }
        exception.attach(this);
        this.exception = exception;
    }

    public boolean isEffectivelyCompatible() {
        return compatible || exception != null;
    }
}
