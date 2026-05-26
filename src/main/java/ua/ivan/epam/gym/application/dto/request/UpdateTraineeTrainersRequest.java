package ua.ivan.epam.gym.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateTraineeTrainersRequest(
        @NotBlank @Size(max = 255) String traineeUsername,
        @NotNull List<@NotBlank String> trainerUsernames
) {
}
