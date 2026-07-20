package com.fourbubbles.ropalista.customer.domain;

import com.fourbubbles.ropalista.common.domain.AuditableEntity;
import com.fourbubbles.ropalista.location.domain.Zone;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_address")
public class Address extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false, length = 160)
    private String street;

    @Column(name = "street_number", nullable = false, length = 30)
    private String streetNumber;

    @Column(length = 100)
    private String neighborhood;

    @Column(nullable = false, length = 120)
    private String locality;

    @Column(columnDefinition = "text")
    private String references;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;
}
