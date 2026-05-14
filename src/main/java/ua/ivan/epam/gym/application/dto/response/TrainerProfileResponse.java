package ua.ivan.epam.gym.application.dto.response;

import java.util.List;

public record TrainerProfileResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization,
        Boolean isActive,
        List<TraineeShortResponse> trainees
) {
}