package ar.com.ropalista.reception.domain;

import ar.com.ropalista.catalog.domain.GarmentEquivalence;
import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "reception_items")
public class ReceptionItem extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reception_id", nullable = false)
    private OrderReception reception;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equivalence_id")
    private GarmentEquivalence equivalence;

    @Column(name = "equivalence_code_snapshot", nullable = false, length = 60)
    private String equivalenceCodeSnapshot;

    @Column(name = "equivalence_name_snapshot", nullable = false, length = 160)
    private String equivalenceNameSnapshot;

    @Column(name = "declared_physical_pieces", nullable = false)
    private int declaredPhysicalPieces;

    @Column(name = "actual_physical_pieces", nullable = false)
    private int actualPhysicalPieces;

    @Column(name = "piece_difference", nullable = false)
    private int pieceDifference;

    @Column(name = "damage_detected", nullable = false)
    private boolean damageDetected;

    @Column(name = "stain_detected", nullable = false)
    private boolean stainDetected;

    @Column(length = 1000)
    private String observations;

    public ReceptionItem(GarmentEquivalence equivalence, String code, String name,
                         int declaredPhysicalPieces, int actualPhysicalPieces,
                         boolean damageDetected, boolean stainDetected, String observations) {
        this.equivalence = equivalence;
        this.equivalenceCodeSnapshot = code;
        this.equivalenceNameSnapshot = name;
        this.declaredPhysicalPieces = declaredPhysicalPieces;
        this.actualPhysicalPieces = actualPhysicalPieces;
        this.pieceDifference = actualPhysicalPieces - declaredPhysicalPieces;
        this.damageDetected = damageDetected;
        this.stainDetected = stainDetected;
        this.observations = observations;
    }

    void attach(OrderReception reception) {
        this.reception = reception;
    }
}
