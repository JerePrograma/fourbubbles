package com.fourbubbles.ropalista.payment.infrastructure;

import com.fourbubbles.ropalista.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrderIdAndDeletedAtIsNullOrderByPaidAt(UUID orderId);

    @Query("""
        select coalesce(sum(p.amount), 0)
        from Payment p
        where p.order.id = :orderId
          and p.deletedAt is null
          and p.status <> com.fourbubbles.ropalista.payment.domain.PaymentStatus.CANCELLED
        """)
    BigDecimal totalPaid(UUID orderId);
}
