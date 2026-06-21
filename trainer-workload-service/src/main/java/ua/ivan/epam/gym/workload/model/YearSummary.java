package ua.ivan.epam.gym.workload.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YearSummary {

    private Integer year;

    @Builder.Default
    private List<MonthSummary> months = new ArrayList<>();
}
