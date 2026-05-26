package ua.ivan.epam.gym.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GetTrainerTrainingsRequest(
        @NotBlank @Size(max = 255) String trainerUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
