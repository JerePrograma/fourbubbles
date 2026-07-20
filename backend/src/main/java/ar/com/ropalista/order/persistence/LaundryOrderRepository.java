package ar.com.ropalista.order.persistence;

import ar.com.ropalista.order.domain.LaundryOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface LaundryOrderRepository extends JpaRepository<LaundryOrder, UUID>, JpaSpecificationExecutor<LaundryOrder> {
    Optional<LaundryOrder> findByIdAndDeletedAtIsNull(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from LaundryOrder o where o.id = :id and o.deletedAt is null")
    Optional<LaundryOrder> findByIdForUpdate(@Param("id") UUID id);

    boolean existsByClientIdAndDeletedAtIsNull(UUID clientId);
    long countByClientIdAndDeletedAtIsNull(UUID clientId);

    @Override
    @EntityGraph(attributePaths = {"client", "service"})
    Page<LaundryOrder> findAll(Specification<LaundryOrder> specification, Pageable pageable);
}
