package ar.com.ropalista.reception.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.order.domain.LaundryOrder;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_receptions")
public class OrderReception extends AuditableEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private LaundryOrder order;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 120)
    private String idempotencyKey;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "declared_physical_pieces", nullable = false)
    private int declaredPhysicalPieces;

    @Column(name = "actual_physical_pieces", nullable = false)
    private int actualPhysicalPieces;

    @Column(name = "declared_weight_grams")
    private Integer declaredWeightGrams;

    @Column(name = "actual_weight_grams", nullable = false)
    private int actualWeightGrams;

    @Column(name = "piece_difference", nullable = false)
    private int pieceDifference;

    @Column(name = "weight_difference_grams")
    private Integer weightDifferenceGrams;

    @Column(name = "condition_notes", length = 2000)
    private String conditionNotes;

    @Column(name = "damage_detected", nullable = false)
    private boolean damageDetected;

    @Column(name = "stain_detected", nullable = false)
    private boolean stainDetected;

    @Column(name = "requires_customer_approval", nullable = false)
    private boolean requiresCustomerApproval;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    private ReceptionApprovalStatus approvalStatus;

    @Column(name = "approval_at")
    private OffsetDateTime approvalAt;

    @Column(name = "approval_by", length = 100)
    private String approvalBy;

    @Column(name = "approval_notes", length = 1000)
    private String approvalNotes;

    @Column(name = "label_code", nullable = false, unique = true, length = 30)
    private String labelCode;

    @Column(name = "bag_code", length = 100)
    private String bagCode;

    @OneToMany(mappedBy = "reception", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceptionItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "reception", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReceptionEvidence> evidences = new ArrayList<>();

    public OrderReception(LaundryOrder order, String idempotencyKey, OffsetDateTime receivedAt,
                          int declaredPhysicalPieces, int actualPhysicalPieces,
                          Integer declaredWeightGrams, int actualWeightGrams,
                          String conditionNotes, boolean damageDetected, boolean stainDetected,
                          boolean requiresCustomerApproval, String labelCode, String bagCode) {
        this.order = order;
        this.idempotencyKey = idempotencyKey;
        this.receivedAt = receivedAt;
        this.declaredPhysicalPieces = declaredPhysicalPieces;
        this.actualPhysicalPieces = actualPhysicalPieces;
        this.declaredWeightGrams = declaredWeightGrams;
        this.actualWeightGrams = actualWeightGrams;
        this.pieceDifference = actualPhysicalPieces - declaredPhysicalPieces;
        this.weightDifferenceGrams = declaredWeightGrams == null ? null : actualWeightGrams - declaredWeightGrams;
        this.conditionNotes = conditionNotes;
        this.damageDetected = damageDetected;
        this.stainDetected = stainDetected;
        this.requiresCustomerApproval = requiresCustomerApproval;
        this.approvalStatus = requiresCustomerApproval
                ? ReceptionApprovalStatus.PENDING
                : ReceptionApprovalStatus.NOT_REQUIRED;
        this.labelCode = labelCode;
        this.bagCode = bagCode;
    }

    public void addItem(ReceptionItem item) {
        item.attach(this);
        items.add(item);
    }

    public void addEvidence(ReceptionEvidence evidence) {
        evidence.attach(this);
        evidences.add(evidence);
    }

    public void decide(ReceptionApprovalStatus decision, String actor, OffsetDateTime at, String notes) {
        if (!requiresCustomerApproval || approvalStatus != ReceptionApprovalStatus.PENDING) {
            throw new IllegalStateException("La recepción no tiene una aprobación pendiente");
        }
        if (decision != ReceptionApprovalStatus.APPROVED && decision != ReceptionApprovalStatus.REJECTED) {
            throw new IllegalArgumentException("La decisión debe ser APPROVED o REJECTED");
        }
        this.approvalStatus = decision;
        this.approvalBy = actor;
        this.approvalAt = at;
        this.approvalNotes = notes;
    }
}
