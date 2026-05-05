package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record GetTrainerTrainingsRequest(
        @NotBlank String trainerUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String traineeName
) {
}
