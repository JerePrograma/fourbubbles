package ar.com.ropalista.pricing.domain;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.location.domain.Zone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "price_definitions")
public class PriceDefinition extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceOffering service;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    @Column(name = "customer_type", length = 40)
    private String customerType;
    @Column(length = 40)
    private String channel;
    @Column(length = 40)
    private String modality;
    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;
    @Column(name = "valid_to")
    private OffsetDateTime validTo;
    @Column(nullable = false, length = 500)
    private String reason;
    @Column(nullable = false)
    private boolean active = true;
}
