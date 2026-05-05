package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotBlank @Size(max = 255) String traineeUsername,
        @NotNull List<String> trainerUsernames
) {
}
