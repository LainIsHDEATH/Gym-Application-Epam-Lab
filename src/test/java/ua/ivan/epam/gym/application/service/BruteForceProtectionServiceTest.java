package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.LoginAttempt;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.LoginAttemptRepository;
import ua.ivan.epam.gym.application.repository.UserRepository;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BruteForceProtectionServiceTest {

    @Mock
    private LoginAttemptRepository loginAttemptRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BruteForceProtectionService bruteForceProtectionService;

    @Test
    void isBlockedShouldReturnFalseWhenLoginAttemptDoesNotExist() {
        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.empty());

        boolean result = bruteForceProtectionService.isBlocked("John.Smith");

        assertFalse(result);

        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository, never()).delete(any());
    }

    @Test
    void isBlockedShouldReturnFalseWhenBlockedUntilIsNull() {
        LoginAttempt attempt = LoginAttempt.builder()
                .user(createUser("John.Smith"))
                .failedAttempts(1)
                .blockedUntil(null)
                .lastFailedAt(Instant.now())
                .build();

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.of(attempt));

        boolean result = bruteForceProtectionService.isBlocked("John.Smith");

        assertFalse(result);

        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository, never()).delete(any());
    }

    @Test
    void isBlockedShouldReturnFalseAndDeleteAttemptWhenBlockExpired() {
        LoginAttempt attempt = LoginAttempt.builder()
                .user(createUser("John.Smith"))
                .failedAttempts(3)
                .blockedUntil(Instant.now().minusSeconds(60))
                .lastFailedAt(Instant.now().minusSeconds(300))
                .build();

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.of(attempt));

        boolean result = bruteForceProtectionService.isBlocked("John.Smith");

        assertFalse(result);

        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository).delete(attempt);
    }

    @Test
    void isBlockedShouldReturnTrueWhenBlockIsActive() {
        LoginAttempt attempt = LoginAttempt.builder()
                .user(createUser("John.Smith"))
                .failedAttempts(3)
                .blockedUntil(Instant.now().plusSeconds(300))
                .lastFailedAt(Instant.now())
                .build();

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.of(attempt));

        boolean result = bruteForceProtectionService.isBlocked("John.Smith");

        assertTrue(result);

        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository, never()).delete(any());
    }

    @Test
    void loginSucceededShouldDeleteExistingLoginAttempt() {
        LoginAttempt attempt = LoginAttempt.builder()
                .user(createUser("John.Smith"))
                .failedAttempts(2)
                .lastFailedAt(Instant.now())
                .build();

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.of(attempt));

        bruteForceProtectionService.loginSucceeded("John.Smith");

        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository).delete(attempt);
    }

    @Test
    void loginSucceededShouldDoNothingWhenLoginAttemptDoesNotExist() {
        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.empty());

        bruteForceProtectionService.loginSucceeded("John.Smith");

        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository, never()).delete(any());
    }

    @Test
    void loginFailedShouldDoNothingWhenUserDoesNotExist() {
        when(userRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        bruteForceProtectionService.loginFailed("Unknown.User");

        verify(userRepository).findByUsername("Unknown.User");
        verifyNoInteractions(loginAttemptRepository);
    }

    @Test
    void loginFailedShouldCreateLoginAttemptWhenAttemptDoesNotExist() {
        User user = createUser("John.Smith");

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.empty());

        when(loginAttemptRepository.save(any(LoginAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bruteForceProtectionService.loginFailed("John.Smith");

        verify(userRepository).findByUsername("John.Smith");
        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");

        verify(loginAttemptRepository).save(argThat(attempt ->
                attempt.getUser() == user
                        && attempt.getFailedAttempts() == 1
                        && attempt.getBlockedUntil() == null
                        && attempt.getLastFailedAt() != null
        ));
    }

    @Test
    void loginFailedShouldIncrementExistingAttemptWhenMaxAttemptsNotReached() {
        User user = createUser("John.Smith");

        LoginAttempt attempt = LoginAttempt.builder()
                .user(user)
                .failedAttempts(1)
                .lastFailedAt(Instant.now().minusSeconds(60))
                .build();

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.of(attempt));

        when(loginAttemptRepository.save(any(LoginAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bruteForceProtectionService.loginFailed("John.Smith");

        assertEquals(2, attempt.getFailedAttempts());
        assertNull(attempt.getBlockedUntil());
        assertNotNull(attempt.getLastFailedAt());

        verify(userRepository).findByUsername("John.Smith");
        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository).save(attempt);
    }

    @Test
    void loginFailedShouldBlockUserWhenMaxAttemptsReached() {
        User user = createUser("John.Smith");

        LoginAttempt attempt = LoginAttempt.builder()
                .user(user)
                .failedAttempts(2)
                .lastFailedAt(Instant.now().minusSeconds(60))
                .build();

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.of(attempt));

        when(loginAttemptRepository.save(any(LoginAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();

        bruteForceProtectionService.loginFailed("John.Smith");

        Instant after = Instant.now();

        assertEquals(3, attempt.getFailedAttempts());
        assertNotNull(attempt.getBlockedUntil());
        assertFalse(attempt.getBlockedUntil().isBefore(before.plusSeconds(299)));
        assertFalse(attempt.getBlockedUntil().isAfter(after.plusSeconds(301)));

        verify(userRepository).findByUsername("John.Smith");
        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository).save(attempt);
    }

    @Test
    void loginFailedShouldKeepUserBlockedWhenAttemptsExceedMaxAttempts() {
        User user = createUser("John.Smith");

        LoginAttempt attempt = LoginAttempt.builder()
                .user(user)
                .failedAttempts(3)
                .blockedUntil(Instant.now().plusSeconds(200))
                .lastFailedAt(Instant.now().minusSeconds(60))
                .build();

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        when(loginAttemptRepository.findByUsernameForUpdate("John.Smith"))
                .thenReturn(Optional.of(attempt));

        when(loginAttemptRepository.save(any(LoginAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bruteForceProtectionService.loginFailed("John.Smith");

        assertEquals(4, attempt.getFailedAttempts());
        assertNotNull(attempt.getBlockedUntil());
        assertTrue(attempt.getBlockedUntil().isAfter(Instant.now()));

        verify(userRepository).findByUsername("John.Smith");
        verify(loginAttemptRepository).findByUsernameForUpdate("John.Smith");
        verify(loginAttemptRepository).save(attempt);
    }

    private User createUser(String username) {
        String[] parts = username.split("\\.");

        return User.builder()
                .id(1L)
                .firstName(parts[0])
                .lastName(parts[1])
                .username(username)
                .password("encodedPassword")
                .isActive(true)
                .build();
    }
}