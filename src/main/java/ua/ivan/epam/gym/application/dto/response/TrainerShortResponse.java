package ua.ivan.epam.gym.application.dto.response;

public record TrainerShortResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeResponse specialization
) {
}