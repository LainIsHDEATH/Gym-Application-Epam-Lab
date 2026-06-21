package ua.ivan.epam.gym.workload.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workloads")
@CompoundIndex(
        name = "idx_trainer_first_name_last_name",
        def = "{'trainerFirstName': 1, 'trainerLastName': 1}"
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkload {

    @Id
    private String id;

    @Indexed(unique = true)
    private String trainerUsername;

    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;

    @Builder.Default
    private List<YearSummary> years = new ArrayList<>();

    public void addDuration(int year, int month, int duration) {
        MonthSummary monthSummary = getOrCreateMonth(year, month);
        monthSummary.setTrainingSummaryDuration(
                monthSummary.getTrainingSummaryDuration() + duration
        );
    }

    public void subtractDuration(int year, int month, int duration) {
        MonthSummary monthSummary = getOrCreateMonth(year, month);
        int updated = monthSummary.getTrainingSummaryDuration() - duration;
        monthSummary.setTrainingSummaryDuration(Math.max(updated, 0));
    }

    public int getDuration(int year, int month) {
        return years.stream()
                .filter(yearSummary -> yearSummary.getYear().equals(year))
                .findFirst()
                .flatMap(yearSummary -> yearSummary.getMonths().stream()
                        .filter(monthSummary -> monthSummary.getMonth().equals(month))
                        .findFirst())
                .map(MonthSummary::getTrainingSummaryDuration)
                .orElse(0);
    }

    public void updateTrainerInfo(String firstName, String lastName, Boolean isActive) {
        this.trainerFirstName = firstName;
        this.trainerLastName = lastName;
        this.isActive = isActive;
    }

    private MonthSummary getOrCreateMonth(int year, int month) {
        YearSummary yearSummary = years.stream()
                .filter(existingYear -> existingYear.getYear().equals(year))
                .findFirst()
                .orElseGet(() -> {
                    YearSummary createdYear = YearSummary.builder()
                            .year(year)
                            .months(new ArrayList<>())
                            .build();
                    years.add(createdYear);
                    return createdYear;
                });

        return yearSummary.getMonths().stream()
                .filter(existingMonth -> existingMonth.getMonth().equals(month))
                .findFirst()
                .orElseGet(() -> {
                    MonthSummary createdMonth = MonthSummary.builder()
                            .month(month)
                            .trainingSummaryDuration(0)
                            .build();
                    yearSummary.getMonths().add(createdMonth);
                    return createdMonth;
                });
    }
}