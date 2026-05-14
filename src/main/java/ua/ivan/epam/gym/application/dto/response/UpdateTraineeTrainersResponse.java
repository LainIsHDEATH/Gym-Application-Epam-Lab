package ua.ivan.epam.gym.application.dto.response;

import java.util.List;

public record UpdateTraineeTrainersResponse(
        List<TrainerShortResponse> trainers
) {
}
