package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GetTraineeTrainingsRequest(
        @NotBlank @Size(max = 255) String traineeUsername,
        LocalDate fromDate,
        LocalDate toDate,
        String trainerName,
        Long trainingTypeId
) {
}