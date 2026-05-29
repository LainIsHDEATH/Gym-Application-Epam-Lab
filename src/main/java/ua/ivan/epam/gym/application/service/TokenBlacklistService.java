package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.model.BlacklistedToken;
import ua.ivan.epam.gym.application.repository.BlacklistedTokenRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Transactional
    public void blacklist(String token, Instant expiresAt) {
        if (token == null || token.isBlank() || expiresAt == null || expiresAt.isBefore(Instant.now())) {
            return;
        }

        String tokenHash = hashToken(token);

        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        return blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
                hashToken(token),
                Instant.now()
        );
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}