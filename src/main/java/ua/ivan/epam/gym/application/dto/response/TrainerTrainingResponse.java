package ua.ivan.epam.gym.application.dto.response;

import java.time.LocalDate;

public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeResponse trainingType,
        Integer trainingDuration,
        String traineeName
) {
}
