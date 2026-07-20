package com.fourbubbles.ropalista.auth.infrastructure;

import com.fourbubbles.ropalista.auth.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);
}
