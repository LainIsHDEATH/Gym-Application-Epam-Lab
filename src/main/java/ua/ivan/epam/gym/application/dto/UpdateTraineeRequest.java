package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateTraineeRequest(
        @NotNull Long traineeId,
        LocalDate dateOfBirth,
        String address
) {
}
