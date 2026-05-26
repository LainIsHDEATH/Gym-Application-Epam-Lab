package ua.ivan.epam.gym.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AddTrainingRequest(
        @NotBlank String traineeUsername,
        @NotBlank String trainerUsername,
        @NotBlank @Size(max = 255) String trainingName,
        @NotNull LocalDate trainingDate,
        @NotNull @Positive Integer trainingDuration
) {
}
