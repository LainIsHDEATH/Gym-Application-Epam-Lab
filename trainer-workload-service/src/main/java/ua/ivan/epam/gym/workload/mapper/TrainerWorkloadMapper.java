package ua.ivan.epam.gym.workload.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.MonthSummaryResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.YearSummaryResponse;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {

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

    default List<YearSummaryResponse> toYearSummaryResponses(Map<Integer, Map<Integer, Integer>> years) {
        return years.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(yearEntry -> new YearSummaryResponse(
                        yearEntry.getKey(),
                        toMonthSummaryResponses(yearEntry.getValue())
                ))
                .toList();
    }

    default List<MonthSummaryResponse> toMonthSummaryResponses(Map<Integer, Integer> months) {
        return months.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(monthEntry -> new MonthSummaryResponse(
                        monthEntry.getKey(),
                        monthEntry.getValue()
                ))
                .toList();
    }
}