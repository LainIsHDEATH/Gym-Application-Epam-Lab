package ua.ivan.epam.gym.workload.dto.response;

import java.util.List;

public record TrainerWorkloadResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        List<YearSummaryResponse> years
) {
}