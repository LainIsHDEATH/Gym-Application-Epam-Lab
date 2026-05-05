package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotBlank String traineeUsername,
        @NotNull List<String> trainerUsernames
) {
}
