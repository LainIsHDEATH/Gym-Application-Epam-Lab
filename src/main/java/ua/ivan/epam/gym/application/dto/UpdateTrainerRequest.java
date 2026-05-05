package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateTrainerRequest(
        @NotNull Long trainerId,
        @NotNull Long specializationId) {
}
