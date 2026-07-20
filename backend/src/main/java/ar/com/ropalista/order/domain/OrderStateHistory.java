package ar.com.ropalista.order.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_state_history")
public class OrderStateHistory extends AuditableEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private LaundryOrder order;
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 40)
    private OrderStatus previousStatus;
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 40)
    private OrderStatus newStatus;
    @Column(length = 1000)
    private String observation;
    @Column(length = 200)
    private String location;
    @Column(name = "notification_reference", length = 200)
    private String notificationReference;

    public OrderStateHistory(LaundryOrder order, OrderStatus previousStatus, OrderStatus newStatus,
                             String observation, String location, String notificationReference) {
        this.order = order;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.observation = observation;
        this.location = location;
        this.notificationReference = notificationReference;
    }
}
