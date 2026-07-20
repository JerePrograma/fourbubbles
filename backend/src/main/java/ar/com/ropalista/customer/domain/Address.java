package ar.com.ropalista.customer.domain;

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

@Getter
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class Address extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
    @Column(nullable = false, length = 160)
    private String street;
    @Column(nullable = false, length = 20)
    private String number;
    @Column(length = 120)
    private String extra;
    @Column(nullable = false, length = 120)
    private String locality;
    @Column(length = 120)
    private String neighborhood;
    @Column(name = "delivery_references", length = 500)
    private String references;
    @Column(name = "is_primary", nullable = false)
    private boolean primaryAddress;
    @Column(nullable = false)
    private boolean active = true;

    public Address(Zone zone, String street, String number, String extra, String locality,
                   String neighborhood, String references, boolean primaryAddress) {
        this.zone = zone;
        this.street = street;
        this.number = number;
        this.extra = extra;
        this.locality = locality;
        this.neighborhood = neighborhood;
        this.references = references;
        this.primaryAddress = primaryAddress;
    }

    void attachTo(Client client) {
        this.client = client;
    }
}
