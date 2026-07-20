package ar.com.ropalista.reception.domain;

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
@Table(name = "reception_evidences")
public class ReceptionEvidence extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reception_id", nullable = false)
    private OrderReception reception;

    @Column(name = "object_key", nullable = false, unique = true, length = 500)
    private String objectKey;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(length = 500)
    private String caption;

    public ReceptionEvidence(String objectKey, String fileName, String contentType,
                             long sizeBytes, String sha256, String caption) {
        this.objectKey = objectKey;
        this.fileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256 = sha256.toLowerCase(java.util.Locale.ROOT);
        this.caption = caption;
    }

    void attach(OrderReception reception) {
        this.reception = reception;
    }
}
