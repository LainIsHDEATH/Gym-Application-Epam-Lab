package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class TokenBlacklistService {

    private static final int TOKEN_HASH_LOG_PREFIX_LENGTH = 8;

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Transactional
    public void blacklist(String token, Instant expiresAt) {
        if (token == null || token.isBlank()) {
            log.warn("Cannot blacklist token. Token is missing");
            return;
        }

        if (expiresAt == null) {
            log.warn("Cannot blacklist token. Expiration date is missing");
            return;
        }

        if (expiresAt.isBefore(Instant.now())) {
            log.info("Skipping blacklist for expired token. expiresAt={}", expiresAt);
            return;
        }

        String tokenHash = hashToken(token);

        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .build();

        blacklistedTokenRepository.save(blacklistedToken);

        log.info("Token blacklisted. tokenHashPrefix={}, expiresAt={}",
                tokenHashPrefix(tokenHash), expiresAt);
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            log.debug("Token blacklist check skipped. Token is missing");
            return false;
        }

        String tokenHash = hashToken(token);

        boolean blacklisted = blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
                tokenHash,
                Instant.now()
        );

        if (blacklisted) {
            log.warn("Blacklisted token usage detected. tokenHashPrefix={}",
                    tokenHashPrefix(tokenHash));
        } else {
            log.debug("Token is not blacklisted. tokenHashPrefix={}",
                    tokenHashPrefix(tokenHash));
        }

        return blacklisted;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            log.error("Cannot hash token. SHA-256 algorithm is not available", exception);
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private String tokenHashPrefix(String tokenHash) {
        if (tokenHash == null || tokenHash.length() <= TOKEN_HASH_LOG_PREFIX_LENGTH) {
            return tokenHash;
        }

        return tokenHash.substring(0, TOKEN_HASH_LOG_PREFIX_LENGTH);
    }
}