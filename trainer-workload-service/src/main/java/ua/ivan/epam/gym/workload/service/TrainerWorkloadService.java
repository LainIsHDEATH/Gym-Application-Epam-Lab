package ua.ivan.epam.gym.workload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.exception.exceptions.EntityNotFoundException;
import ua.ivan.epam.gym.workload.mapper.TrainerWorkloadMapper;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;
import ua.ivan.epam.gym.workload.repository.TrainerWorkloadRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadService {

    private final TrainerWorkloadRepository trainerWorkloadRepository;
    private final TrainerWorkloadMapper trainerWorkloadMapper;

    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        LocalDate trainingDate = request.trainingDate();

        int year = trainingDate.getYear();
        int month = trainingDate.getMonthValue();

        TrainerWorkload workload = trainerWorkloadRepository
                .findByTrainerUsername(request.trainerUsername())
                .orElseGet(() -> {
                    log.info("Trainer workload document not found. Creating new document. trainerUsername={}",
                            request.trainerUsername());
                    return trainerWorkloadMapper.toEntity(request);
                });

        workload.updateTrainerInfo(
                request.trainerFirstName(),
                request.trainerLastName(),
                request.isActive()
        );

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
        TrainerWorkload workload = findWorkloadByUsername(username);

        return trainerWorkloadMapper.toMonthlyResponse(workload, year, month);
    }

    public TrainerWorkloadResponse getTrainerWorkload(String username) {
        TrainerWorkload workload = findWorkloadByUsername(username);

        return trainerWorkloadMapper.toResponse(workload);
    }

    private TrainerWorkload findWorkloadByUsername(String username) {
        return trainerWorkloadRepository.findByTrainerUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainer workload not found. trainerUsername={}", username);
                    return new EntityNotFoundException("Trainer workload not found. username=" + username);
                });
    }
}