package ua.ivan.epam.gym.workload.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummary {

    private Integer month;

    private Integer trainingSummaryDuration;
}