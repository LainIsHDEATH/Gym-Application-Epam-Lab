package ua.ivan.epam.gym.application.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    /* TODO implement a table or a column in DB*/
    private final Map<String, Instant> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, Instant expiresAt) {
        if (expiresAt == null) {
            return;
        }

        blacklistedTokens.put(token, expiresAt);
    }

    public boolean isBlacklisted(String token) {
        Instant expiresAt = blacklistedTokens.get(token);

        if (expiresAt == null) {
            return false;
        }

        if (expiresAt.isBefore(Instant.now())) {
            blacklistedTokens.remove(token);
            return false;
        }

        return true;
    }
}