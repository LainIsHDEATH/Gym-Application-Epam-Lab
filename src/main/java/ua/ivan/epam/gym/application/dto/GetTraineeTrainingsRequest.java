package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record GetTraineeTrainingsRequest(
        @NotBlank String traineeUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        Long trainingTypeId
) {
}