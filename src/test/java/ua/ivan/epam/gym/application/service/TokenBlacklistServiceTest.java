package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.BlacklistedToken;
import ua.ivan.epam.gym.application.repository.BlacklistedTokenRepository;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private BlacklistedTokenRepository blacklistedTokenRepository;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void blacklistShouldSaveTokenHashWhenTokenAndExpirationAreValid() {
        String token = "valid.jwt.token";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        tokenBlacklistService.blacklist(token, expiresAt);

        ArgumentCaptor<BlacklistedToken> captor =
                ArgumentCaptor.forClass(BlacklistedToken.class);

        verify(blacklistedTokenRepository).save(captor.capture());

        BlacklistedToken savedToken = captor.getValue();

        assertNotNull(savedToken);
        assertNotNull(savedToken.getTokenHash());
        assertEquals(64, savedToken.getTokenHash().length());
        assertEquals(expiresAt, savedToken.getExpiresAt());

        assertNotEquals(token, savedToken.getTokenHash());
    }

    @Test
    void blacklistShouldNotSaveWhenTokenIsNull() {
        Instant expiresAt = Instant.now().plusSeconds(3600);

        tokenBlacklistService.blacklist(null, expiresAt);

        verifyNoInteractions(blacklistedTokenRepository);
    }

    @Test
    void blacklistShouldNotSaveWhenTokenIsBlank() {
        Instant expiresAt = Instant.now().plusSeconds(3600);

        tokenBlacklistService.blacklist("   ", expiresAt);

        verifyNoInteractions(blacklistedTokenRepository);
    }

    @Test
    void blacklistShouldNotSaveWhenExpirationIsNull() {
        tokenBlacklistService.blacklist("valid.jwt.token", null);

        verifyNoInteractions(blacklistedTokenRepository);
    }

    @Test
    void blacklistShouldNotSaveWhenTokenIsAlreadyExpired() {
        Instant expiresAt = Instant.now().minusSeconds(60);

        tokenBlacklistService.blacklist("valid.jwt.token", expiresAt);

        verifyNoInteractions(blacklistedTokenRepository);
    }

    @Test
    void isBlacklistedShouldReturnFalseWhenTokenIsNull() {
        boolean result = tokenBlacklistService.isBlacklisted(null);

        assertFalse(result);

        verifyNoInteractions(blacklistedTokenRepository);
    }

    @Test
    void isBlacklistedShouldReturnFalseWhenTokenIsBlank() {
        boolean result = tokenBlacklistService.isBlacklisted("   ");

        assertFalse(result);

        verifyNoInteractions(blacklistedTokenRepository);
    }

    @Test
    void isBlacklistedShouldReturnTrueWhenRepositoryFindsActiveBlacklistedToken() {
        String token = "valid.jwt.token";

        when(blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(true);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertTrue(result);

        verify(blacklistedTokenRepository).existsByTokenHashAndExpiresAtAfter(
                argThat(tokenHash -> tokenHash != null
                        && tokenHash.length() == 64
                        && !tokenHash.equals(token)),
                any(Instant.class)
        );
    }

    @Test
    void isBlacklistedShouldReturnFalseWhenRepositoryDoesNotFindActiveBlacklistedToken() {
        String token = "valid.jwt.token";

        when(blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(false);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertFalse(result);

        verify(blacklistedTokenRepository).existsByTokenHashAndExpiresAtAfter(
                argThat(tokenHash -> tokenHash != null
                        && tokenHash.length() == 64
                        && !tokenHash.equals(token)),
                any(Instant.class)
        );
    }

    @Test
    void sameTokenShouldProduceSameHashForBlacklistAndCheck() {
        String token = "valid.jwt.token";
        Instant expiresAt = Instant.now().plusSeconds(3600);

        tokenBlacklistService.blacklist(token, expiresAt);

        ArgumentCaptor<BlacklistedToken> saveCaptor =
                ArgumentCaptor.forClass(BlacklistedToken.class);

        verify(blacklistedTokenRepository).save(saveCaptor.capture());

        String savedHash = saveCaptor.getValue().getTokenHash();

        when(blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(true);

        boolean result = tokenBlacklistService.isBlacklisted(token);

        assertTrue(result);

        verify(blacklistedTokenRepository).existsByTokenHashAndExpiresAtAfter(
                eq(savedHash),
                any(Instant.class)
        );
    }

    @Test
    void differentTokensShouldProduceDifferentHashes() {
        Instant expiresAt = Instant.now().plusSeconds(3600);

        tokenBlacklistService.blacklist("first.jwt.token", expiresAt);
        tokenBlacklistService.blacklist("second.jwt.token", expiresAt);

        ArgumentCaptor<BlacklistedToken> captor =
                ArgumentCaptor.forClass(BlacklistedToken.class);

        verify(blacklistedTokenRepository, times(2)).save(captor.capture());

        BlacklistedToken first = captor.getAllValues().get(0);
        BlacklistedToken second = captor.getAllValues().get(1);

        assertNotEquals(first.getTokenHash(), second.getTokenHash());
        assertEquals(64, first.getTokenHash().length());
        assertEquals(64, second.getTokenHash().length());
    }
}