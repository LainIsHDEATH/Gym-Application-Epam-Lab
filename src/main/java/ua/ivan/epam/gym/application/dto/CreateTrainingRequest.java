package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateTrainingRequest(
        @NotNull Long traineeId,
        @NotNull Long trainerId,
        @NotBlank @Size(max = 255) String trainingName,
        @NotNull Long trainingTypeId,
        @NotNull LocalDate trainingDate,
        @Positive int trainingDuration
) {
}