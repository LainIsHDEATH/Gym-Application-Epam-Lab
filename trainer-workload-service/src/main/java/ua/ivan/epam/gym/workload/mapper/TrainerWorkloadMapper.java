package ua.ivan.epam.gym.workload.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.MonthSummaryResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.YearSummaryResponse;
import ua.ivan.epam.gym.workload.model.MonthSummary;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;
import ua.ivan.epam.gym.workload.model.YearSummary;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "years", ignore = true)
    TrainerWorkload toEntity(TrainerWorkloadRequest request);

    @Mapping(source = "workload.trainerUsername", target = "trainerUsername")
    @Mapping(source = "workload.trainerFirstName", target = "trainerFirstName")
    @Mapping(source = "workload.trainerLastName", target = "trainerLastName")
    @Mapping(source = "workload.isActive", target = "isActive")
    @Mapping(source = "year", target = "year")
    @Mapping(source = "month", target = "month")
    @Mapping(target = "trainingSummaryDuration", expression = "java(workload.getDuration(year, month))")
    TrainerMonthlyWorkloadResponse toMonthlyResponse(
            TrainerWorkload workload,
            int year,
            int month
    );

    @Mapping(source = "trainerUsername", target = "trainerUsername")
    @Mapping(source = "trainerFirstName", target = "trainerFirstName")
    @Mapping(source = "trainerLastName", target = "trainerLastName")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(target = "years", expression = "java(toYearSummaryResponses(workload.getYears()))")
    TrainerWorkloadResponse toResponse(TrainerWorkload workload);

    default List<YearSummaryResponse> toYearSummaryResponses(List<YearSummary> years) {
        return years.stream()
                .sorted((y1, y2) -> Integer.compare(y2.getYear(), y1.getYear()))
                .map(yearSummary -> new YearSummaryResponse(
                        yearSummary.getYear(),
                        toMonthSummaryResponses(yearSummary.getMonths())
                ))
                .toList();
    }

    default List<MonthSummaryResponse> toMonthSummaryResponses(List<MonthSummary> months) {
        return months.stream()
                .sorted((m1, m2) -> Integer.compare(m2.getMonth(), m1.getMonth()))
                .map(monthEntry -> new MonthSummaryResponse(
                        monthEntry.getMonth(),
                        monthEntry.getTrainingSummaryDuration()
                ))
                .toList();
    }
}