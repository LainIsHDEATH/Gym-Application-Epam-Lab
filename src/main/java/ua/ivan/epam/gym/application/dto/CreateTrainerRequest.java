package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ua.ivan.epam.gym.application.model.TrainingType;

public record CreateTrainerRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Long specializationId
) {
}