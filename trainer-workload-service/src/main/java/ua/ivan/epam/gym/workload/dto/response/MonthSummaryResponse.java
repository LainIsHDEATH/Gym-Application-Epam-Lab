package ua.ivan.epam.gym.workload.dto.response;

public record MonthSummaryResponse(
        Integer month,
        Integer trainingSummaryDuration
) {
}