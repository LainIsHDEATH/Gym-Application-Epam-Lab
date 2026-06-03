package ua.ivan.epam.gym.application.dto.response;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresInSeconds
) {
}