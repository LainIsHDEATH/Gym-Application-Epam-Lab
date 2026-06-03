package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import ua.ivan.epam.gym.application.dto.request.LoginRequest;
import ua.ivan.epam.gym.application.dto.response.LoginResponse;
import ua.ivan.epam.gym.application.exception.exceptions.AuthenticationException;
import ua.ivan.epam.gym.application.exception.exceptions.UserBlockedException;
import ua.ivan.epam.gym.application.security.GymUserDetailsService;
import ua.ivan.epam.gym.application.security.JwtUtils;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private GymUserDetailsService userDetailsService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginShouldReturnTokenWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("John.Smith", "password12");

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("John.Smith")
                .password("encodedPassword")
                .authorities("ROLE_USER")
                .build();

        when(bruteForceProtectionService.isBlocked("John.Smith"))
                .thenReturn(false);

        when(userDetailsService.loadUserByUsername("John.Smith"))
                .thenReturn(userDetails);

        when(jwtUtils.generateToken(userDetails))
                .thenReturn("jwt-token");

        when(jwtUtils.getExpirationSeconds())
                .thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertEquals("jwt-token", response.token());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresInSeconds());

        verify(bruteForceProtectionService).isBlocked("John.Smith");

        verify(authenticationManager).authenticate(argThat(authentication ->
                authentication instanceof UsernamePasswordAuthenticationToken
                        && authentication.getPrincipal().equals("John.Smith")
                        && authentication.getCredentials().equals("password12")
        ));

        verify(bruteForceProtectionService).loginSucceeded("John.Smith");
        verify(userDetailsService).loadUserByUsername("John.Smith");
        verify(jwtUtils).generateToken(userDetails);
        verify(jwtUtils).getExpirationSeconds();

        verify(bruteForceProtectionService, never()).loginFailed(anyString());
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void loginShouldThrowUserBlockedExceptionWhenUserIsBlocked() {
        LoginRequest request = new LoginRequest("John.Smith", "password12");

        when(bruteForceProtectionService.isBlocked("John.Smith"))
                .thenReturn(true);

        UserBlockedException exception = assertThrows(
                UserBlockedException.class,
                () -> authService.login(request)
        );

        assertEquals("User is temporarily blocked. Try again later", exception.getMessage());

        verify(bruteForceProtectionService).isBlocked("John.Smith");

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(tokenBlacklistService);

        verify(bruteForceProtectionService, never()).loginFailed(anyString());
        verify(bruteForceProtectionService, never()).loginSucceeded(anyString());
    }

    @Test
    void loginShouldThrowAuthenticationExceptionAndRegisterFailedAttemptWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("John.Smith", "wrongPassword");

        when(bruteForceProtectionService.isBlocked("John.Smith"))
                .thenReturn(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(bruteForceProtectionService).isBlocked("John.Smith");

        verify(authenticationManager).authenticate(argThat(authentication ->
                authentication instanceof UsernamePasswordAuthenticationToken
                        && authentication.getPrincipal().equals("John.Smith")
                        && authentication.getCredentials().equals("wrongPassword")
        ));

        verify(bruteForceProtectionService).loginFailed("John.Smith");

        verify(bruteForceProtectionService, never()).loginSucceeded(anyString());
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void loginShouldThrowAuthenticationExceptionWhenUserIsDisabled() {
        LoginRequest request = new LoginRequest("John.Smith", "password12");

        when(bruteForceProtectionService.isBlocked("John.Smith"))
                .thenReturn(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is disabled"));

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(request)
        );

        assertEquals("User is disabled", exception.getMessage());

        verify(bruteForceProtectionService).isBlocked("John.Smith");

        verify(authenticationManager).authenticate(argThat(authentication ->
                authentication instanceof UsernamePasswordAuthenticationToken
                        && authentication.getPrincipal().equals("John.Smith")
                        && authentication.getCredentials().equals("password12")
        ));

        verify(bruteForceProtectionService, never()).loginFailed(anyString());
        verify(bruteForceProtectionService, never()).loginSucceeded(anyString());

        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void logoutShouldBlacklistTokenWhenAuthorizationHeaderIsValid() {
        String authorizationHeader = "Bearer jwt-token";
        Instant expiration = Instant.parse("2026-05-29T12:00:00Z");

        when(jwtUtils.extractExpiration("jwt-token"))
                .thenReturn(expiration);

        assertDoesNotThrow(() -> authService.logout(authorizationHeader));

        verify(jwtUtils).extractExpiration("jwt-token");
        verify(tokenBlacklistService).blacklist("jwt-token", expiration);

        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(bruteForceProtectionService);
    }

    @Test
    void logoutShouldThrowAuthenticationExceptionWhenAuthorizationHeaderIsMissing() {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.logout(null)
        );

        assertEquals("Missing Bearer token", exception.getMessage());

        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(tokenBlacklistService);
        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(bruteForceProtectionService);
    }

    @Test
    void logoutShouldThrowAuthenticationExceptionWhenAuthorizationHeaderDoesNotStartWithBearer() {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.logout("Basic abc123")
        );

        assertEquals("Missing Bearer token", exception.getMessage());

        verifyNoInteractions(jwtUtils);
        verifyNoInteractions(tokenBlacklistService);
        verifyNoInteractions(authenticationManager);
        verifyNoInteractions(userDetailsService);
        verifyNoInteractions(bruteForceProtectionService);
    }
}