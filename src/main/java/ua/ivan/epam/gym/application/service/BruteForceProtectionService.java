package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                        log.debug("User is not blocked. username={}, failedAttempts={}",
                                username, attempt.getFailedAttempts());
                        return false;
                    }

                    if (blockedUntil.isBefore(now)) {
                        loginAttemptRepository.delete(attempt);

                        log.info("Expired login block removed. username={}, blockedUntil={}",
                                username, blockedUntil);

                        return false;
                    }

                    log.warn("Blocked login attempt detected. username={}, blockedUntil={}",
                            username, blockedUntil);

                    return true;
                })
                .orElseGet(() -> {
                    log.debug("No login attempts found. username={}", username);
                    return false;
                });
    }

    @Transactional
    public void loginSucceeded(String username) {
        loginAttemptRepository.findByUsernameForUpdate(username)
                .ifPresentOrElse(
                        attempt -> {
                            loginAttemptRepository.delete(attempt);

                            log.info("Login attempts reset after successful login. username={}, previousFailedAttempts={}",
                                    username, attempt.getFailedAttempts());
                        },
                        () -> log.debug("Successful login without previous failed attempts. username={}", username)
                );
    }

    @Transactional
    public void loginFailed(String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);

        if (user == null) {
            log.warn("Failed login attempt for unknown user. username={}", username);
            return;
        }

        Instant now = Instant.now();

        LoginAttempt attempt = loginAttemptRepository.findByUsernameForUpdate(username)
                .orElseGet(() -> {
                    log.debug("Creating login attempt record. username={}", username);

                    return LoginAttempt.builder()
                            .user(user)
                            .failedAttempts(0)
                            .lastFailedAt(now)
                            .build();
                });

        int failedAttempts = attempt.getFailedAttempts() + 1;

        attempt.setFailedAttempts(failedAttempts);
        attempt.setLastFailedAt(now);

        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            Instant blockedUntil = now.plus(BLOCK_DURATION);
            attempt.setBlockedUntil(blockedUntil);

            log.warn("User blocked due to failed login attempts. username={}, failedAttempts={}, blockedUntil={}",
                    username, failedAttempts, blockedUntil);
        } else {
            log.warn("Failed login attempt registered. username={}, failedAttempts={}, attemptsLeft={}",
                    username, failedAttempts, MAX_FAILED_ATTEMPTS - failedAttempts);
        }

        loginAttemptRepository.save(attempt);
    }
}