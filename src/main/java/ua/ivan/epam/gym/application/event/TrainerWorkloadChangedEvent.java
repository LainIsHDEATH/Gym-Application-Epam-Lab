package ua.ivan.epam.gym.application.event;

import ua.ivan.epam.gym.application.dto.request.TrainerWorkloadRequest;

public record TrainerWorkloadChangedEvent(
        TrainerWorkloadRequest request
) {
}