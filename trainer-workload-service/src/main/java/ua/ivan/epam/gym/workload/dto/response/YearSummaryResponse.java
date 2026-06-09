package ua.ivan.epam.gym.workload.dto.response;

import java.util.List;

public record YearSummaryResponse(
        Integer year,
        List<MonthSummaryResponse> months
) {
}