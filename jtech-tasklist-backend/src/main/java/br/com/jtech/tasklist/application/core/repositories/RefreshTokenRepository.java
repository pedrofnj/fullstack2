package br.com.jtech.tasklist.application.core.repositories;

import br.com.jtech.tasklist.application.core.domains.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenAndRevokedFalse(String token);

    void deleteByUserId(String userId);
}
