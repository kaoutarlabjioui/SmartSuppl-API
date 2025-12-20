package org.smartsupply.service.implementation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. smartsupply.model.entity.RefreshToken;
import org.smartsupply.model. entity.User;
import org. smartsupply.repository.RefreshTokenRepository;
import org. springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time. LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 jours en ms
    private long refreshTokenExpiration;

    @Transactional
    public RefreshToken createRefreshToken(User user, String jwtRefreshToken) {
        // Supprimer les anciens tokens de l'utilisateur (rotation)
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(jwtRefreshToken)
                .user(user)
                .createdAt(LocalDateTime.now())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .revoked(false)
                .build();

        return refreshTokenRepository. save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Transactional
    public void revokeUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public boolean isValid(RefreshToken refreshToken) {
        return refreshToken != null
                && ! refreshToken.getRevoked()
                && ! refreshToken.isExpired();
    }

    // Nettoyage automatique des tokens expirés (toutes les 24h)
    @Scheduled(cron = "0 0 0 * * ? ")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Nettoyage des refresh tokens expirés...");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}