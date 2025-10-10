package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.application.core.domains.RefreshToken;
import br.com.jtech.tasklist.application.core.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public RefreshToken create(String userId) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUserId(UUID.fromString(userId));
        token.setExpiresAt(Instant.now().plusMillis(refreshExpiration));
        token.setRevoked(false);
        return repository.save(token);
    }

    public Optional<RefreshToken> validate(String token) {
        return repository.findByTokenAndRevokedFalse(token)
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()));
    }

    public void revoke(String token) {
        repository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            repository.save(rt);
        });
    }

    public void revokeByUser(String userId) {
        repository.deleteByUserId(UUID.fromString(userId));
    }
}
