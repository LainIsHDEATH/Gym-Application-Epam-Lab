package ua.ivan.epam.gym.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.MonthSummaryResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.YearSummaryResponse;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;
import ua.ivan.epam.gym.workload.repository.TrainerWorkloadRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;

    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        LocalDate trainingDate = request.trainingDate();

        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();

        TrainerWorkload initialWorkload = TrainerWorkload.builder()
                .trainerUsername(request.trainerUsername())
                .trainerFirstName(request.trainerFirstName())
                .trainerLastName(request.trainerLastName())
                .isActive(request.isActive())
                .build();

        TrainerWorkload workload = trainerWorkloadRepository.getOrCreate(
                request.trainerUsername(),
                initialWorkload
        );

        workload.setTrainerFirstName(request.trainerFirstName());
        workload.setTrainerLastName(request.trainerLastName());
        workload.setIsActive(request.isActive());

        if (request.actionType() == WorkloadActionType.ADD) {
            workload.addDuration(year, month, request.trainingDuration());

            log.info("Trainer workload increased. trainerUsername={}, year={}, month={}, duration={}",
                    request.trainerUsername(), year, month, request.trainingDuration());
        } else if (request.actionType() == WorkloadActionType.DELETE) {
            workload.subtractDuration(year, month, request.trainingDuration());

            log.info("Trainer workload decreased. trainerUsername={}, year={}, month={}, duration={}",
                    request.trainerUsername(), year, month, request.trainingDuration());
        }

        trainerWorkloadRepository.save(workload);
    }

    public TrainerMonthlyWorkloadResponse getMonthlyWorkload(String username, int year, int month) {
        TrainerWorkload workload = trainerWorkloadRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainer workload not found. trainerUsername={}", username);
                    return new IllegalArgumentException("Trainer workload not found. username=" + username);
                });

        return new TrainerMonthlyWorkloadResponse(
                workload.getTrainerUsername(),
                workload.getTrainerFirstName(),
                workload.getTrainerLastName(),
                workload.getIsActive(),
                year,
                month,
                workload.getDuration(year, month)
        );
    }

    public TrainerWorkloadResponse getTrainerWorkload(String username) {
        TrainerWorkload workload = trainerWorkloadRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainer workload not found. trainerUsername={}", username);
                    return new IllegalArgumentException("Trainer workload not found. username=" + username);
                });

        List<YearSummaryResponse> years = workload.getYears()
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(yearEntry -> new YearSummaryResponse(
                        yearEntry.getKey(),
                        yearEntry.getValue()
                                .entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(monthEntry -> new MonthSummaryResponse(
                                        monthEntry.getKey(),
                                        monthEntry.getValue()
                                ))
                                .toList()
                ))
                .toList();

        return new TrainerWorkloadResponse(
                workload.getTrainerUsername(),
                workload.getTrainerFirstName(),
                workload.getTrainerLastName(),
                workload.getIsActive(),
                years
        );
    }
}