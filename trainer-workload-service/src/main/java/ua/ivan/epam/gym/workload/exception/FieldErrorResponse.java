package ua.ivan.epam.gym.workload.exception;

public record FieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}