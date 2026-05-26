package ua.ivan.epam.gym.application.authentication;

import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.exception.exceptions.AuthenticationException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class BasicAuthParser {

    private static final String BASIC_PREFIX = "Basic ";

    public BasicAuthCredentials parse(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new AuthenticationException("Missing Authorization header");
        }

        if (!authorizationHeader.startsWith(BASIC_PREFIX)) {
            throw new AuthenticationException("Authorization header must use Basic scheme");
        }

        String base64Credentials = authorizationHeader.substring(BASIC_PREFIX.length()).trim();

        if (base64Credentials.isBlank()) {
            throw new AuthenticationException("Missing Basic Auth credentials");
        }

        String decodedCredentials;

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            decodedCredentials = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new AuthenticationException("Invalid Basic Auth Base64 value");
        }

        int separatorIndex = decodedCredentials.indexOf(':');

        if (separatorIndex < 0) {
            throw new AuthenticationException("Invalid Basic Auth credentials format");
        }

        String username = decodedCredentials.substring(0, separatorIndex);
        String password = decodedCredentials.substring(separatorIndex + 1);

        if (username.isBlank()) {
            throw new AuthenticationException("Basic Auth username must not be blank");
        }

        if (password.isBlank()) {
            throw new AuthenticationException("Basic Auth password must not be blank");
        }

        return new BasicAuthCredentials(username, password);
    }
}