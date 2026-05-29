package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.model.LoginAttempt;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.LoginAttemptRepository;
import ua.ivan.epam.gym.application.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BruteForceProtectionService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean isBlocked(String username) {
        Instant now = Instant.now();

        return loginAttemptRepository.findByUsernameForUpdate(username)
                .map(attempt -> {
                    Instant blockedUntil = attempt.getBlockedUntil();

                    if (blockedUntil == null) {
                        return false;
                    }

                    if (blockedUntil.isBefore(now)) {
                        loginAttemptRepository.delete(attempt);
                        return false;
                    }

                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void loginSucceeded(String username) {
        loginAttemptRepository.findByUsernameForUpdate(username)
                .ifPresent(loginAttemptRepository::delete);
    }

    @Transactional
    public void loginFailed(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            return;
        }

        Instant now = Instant.now();

        LoginAttempt attempt = loginAttemptRepository.findByUsernameForUpdate(username)
                .orElseGet(() -> LoginAttempt.builder()
                        .user(user)
                        .failedAttempts(0)
                        .lastFailedAt(now)
                        .build());

        int failedAttempts = attempt.getFailedAttempts() + 1;

        attempt.setFailedAttempts(failedAttempts);
        attempt.setLastFailedAt(now);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            attempt.setBlockedUntil(now.plus(BLOCK_DURATION));
        }

        loginAttemptRepository.save(attempt);
    }
}