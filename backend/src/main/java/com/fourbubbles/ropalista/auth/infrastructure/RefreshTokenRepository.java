package com.fourbubbles.ropalista.auth.infrastructure;

import com.fourbubbles.ropalista.auth.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHashAndDeletedAtIsNull(String tokenHash);
}
