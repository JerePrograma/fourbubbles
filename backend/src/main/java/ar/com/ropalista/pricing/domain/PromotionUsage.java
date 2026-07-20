package ar.com.ropalista.pricing.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.customer.domain.Address;
import ar.com.ropalista.customer.domain.Client;
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
@Table(name = "promotion_usages")
public class PromotionUsage extends AuditableEntity {
    @Column(name = "order_id", nullable = false, unique = true)
    private java.util.UUID orderId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    public PromotionUsage(java.util.UUID orderId, Promotion promotion, Client client, Address address) {
        this.orderId = orderId;
        this.promotion = promotion;
        this.client = client;
        this.address = address;
    }
}
