package ua.ivan.epam.gym.application.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    /* TODO implement a table or a column in DB*/
    private final Map<String, LoginAttempt> attemptsByUsername = new ConcurrentHashMap<>();

    public boolean isBlocked(String username) {
        LoginAttempt attempt = attemptsByUsername.get(username);

        if (attempt == null || attempt.blockedUntil() == null) {
            return false;
        }

        if (attempt.blockedUntil().isBefore(Instant.now())) {
            attemptsByUsername.remove(username);
            return false;
        }

        return true;
    }

    public void loginSucceeded(String username) {
        attemptsByUsername.remove(username);
    }

    public void loginFailed(String username) {
        LoginAttempt current = attemptsByUsername.get(username);

        int failedAttempts = current == null
                ? 1
                : current.failedAttempts() + 1;

        Instant blockedUntil = failedAttempts >= MAX_FAILED_ATTEMPTS
                ? Instant.now().plus(BLOCK_DURATION)
                : null;

        attemptsByUsername.put(username, new LoginAttempt(failedAttempts, blockedUntil));
    }

    private record LoginAttempt(
            int failedAttempts,
            Instant blockedUntil
    ) {
    }
}