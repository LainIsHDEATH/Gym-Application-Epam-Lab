package ua.ivan.epam.gym.workload.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorResponse> fieldErrors
) {
    public static ErrorResponse of(int status,
                                   String error,
                                   String message,
                                   String path) {
        return new ErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                message,
                path,
                null
        );
    }

    public static ErrorResponse withFieldErrors(int status,
                                                String error,
                                                String message,
                                                String path,
                                                List<FieldErrorResponse> fieldErrors) {
        return new ErrorResponse(
                OffsetDateTime.now(),
                status,
                error,
                message,
                path,
                fieldErrors
        );
    }
}