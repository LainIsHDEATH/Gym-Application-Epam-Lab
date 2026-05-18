package ua.ivan.epam.gym.application.exception;

public record FieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}