package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.actuator.metrics.CountGymFailure;
import ua.ivan.epam.gym.application.actuator.metrics.GymMetric;
import ua.ivan.epam.gym.application.dto.request.LoginRequest;
import ua.ivan.epam.gym.application.dto.response.LoginResponse;
import ua.ivan.epam.gym.application.exception.exceptions.AuthenticationException;
import ua.ivan.epam.gym.application.exception.exceptions.UserBlockedException;
import ua.ivan.epam.gym.application.security.GymUserDetailsService;
import ua.ivan.epam.gym.application.security.JwtUtils;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationManager authenticationManager;
    private final GymUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final TokenBlacklistService tokenBlacklistService;

    @CountGymFailure(
            value = GymMetric.AUTH_FAILURE,
            exceptions = {
                    AuthenticationException.class,
                    UserBlockedException.class
            }
    )
    public LoginResponse login(LoginRequest request) {
        String username = request.username();

        log.debug("Login attempt started. username={}", username);

        if (bruteForceProtectionService.isBlocked(username)) {
            log.warn("Login rejected because user is blocked. username={}", username);
            throw new UserBlockedException("User is temporarily blocked. Try again later");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            username,
                            request.password()
                    )
            );
        } catch (BadCredentialsException exception) {
            log.warn("Login failed due to invalid credentials. username={}", username);

            bruteForceProtectionService.loginFailed(username);
            throw new AuthenticationException("Invalid username or password");
        } catch (DisabledException exception) {
            log.warn("Login failed because user is disabled. username={}", username);

            throw new AuthenticationException("User is disabled");
        }

        bruteForceProtectionService.loginSucceeded(username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtils.generateToken(userDetails);

        log.info("User authenticated successfully. username={}", username);

        return new LoginResponse(
                token,
                "Bearer",
                jwtUtils.getExpirationSeconds()
        );
    }

    public void logout(String authorizationHeader) {
        log.debug("Logout attempt started");

        String token = extractBearerToken(authorizationHeader);
        Instant expiration = jwtUtils.extractExpiration(token);

        tokenBlacklistService.blacklist(token, expiration);

        log.info("User logged out successfully.");
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            log.warn("Bearer token extraction failed. Authorization header is missing or invalid");
            throw new AuthenticationException("Missing Bearer token");
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}