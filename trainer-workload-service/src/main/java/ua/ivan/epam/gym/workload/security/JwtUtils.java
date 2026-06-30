package ua.ivan.epam.gym.workload.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final JwtDecoder jwtDecoder;

    public String extractSubject(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    public boolean isTokenValid(String token) {
        Jwt jwt = jwtDecoder.decode(token);

        Instant expiresAt = jwt.getExpiresAt();

        return expiresAt != null && expiresAt.isAfter(Instant.now());
    }


    public List<String> extractAuthorities(String token) {
        Jwt jwt = jwtDecoder.decode(token);

        List<String> authorities =
                jwt.getClaimAsStringList("authorities");

        return authorities == null
                ? List.of()
                : authorities;
    }
}