package ua.ivan.epam.gym.workload.dto.response;

public record TrainerMonthlyWorkloadResponse(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        Integer year,
        Integer month,
        Integer trainingSummaryDuration
) {
}