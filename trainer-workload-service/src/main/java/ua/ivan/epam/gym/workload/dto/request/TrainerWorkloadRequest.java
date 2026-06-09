package ua.ivan.epam.gym.workload.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;

import java.time.LocalDate;

public record TrainerWorkloadRequest(
        @NotBlank String trainerUsername,
        @NotBlank String trainerFirstName,
        @NotBlank String trainerLastName,
        @NotNull Boolean isActive,
        @NotNull LocalDate trainingDate,
        @NotNull @Min(1) Integer trainingDuration,
        @NotNull WorkloadActionType actionType
) {
}