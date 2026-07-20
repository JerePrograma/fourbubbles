package ar.com.ropalista.payment.persistence;

import ar.com.ropalista.payment.domain.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.order.id = :orderId and p.status = 'PAID'")
    BigDecimal sumPaidByOrderId(@Param("orderId") UUID orderId);

    @EntityGraph(attributePaths = {"method"})
    List<Payment> findByOrderIdOrderByPaidAtAsc(UUID orderId);
}
