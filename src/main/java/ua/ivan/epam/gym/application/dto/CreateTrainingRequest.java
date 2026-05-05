package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record CreateTrainingRequest(
        @NotNull Long traineeId,
        @NotNull Long trainerId,
        @NotBlank String trainingName,
        @NotNull Long trainingTypeId,
        @NotNull LocalDate trainingDate,
        @Positive int trainingDuration
) {
}