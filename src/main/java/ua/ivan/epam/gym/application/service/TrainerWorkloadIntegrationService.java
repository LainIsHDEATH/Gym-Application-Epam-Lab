package ua.ivan.epam.gym.application.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.ivan.epam.gym.application.client.TrainerWorkloadClient;
import ua.ivan.epam.gym.application.dto.request.TrainerWorkloadRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadIntegrationService {

    private final TrainerWorkloadClient trainerWorkloadClient;

    @CircuitBreaker(
            name = "trainerWorkloadService",
            fallbackMethod = "sendTrainerWorkloadFallback"
    )
    public void sendTrainerWorkload(TrainerWorkloadRequest request) {
        trainerWorkloadClient.updateTrainerWorkload(request);

        log.info("Trainer workload event sent. trainerUsername={}, actionType={}, trainingDate={}, duration={}",
                request.trainerUsername(),
                request.actionType(),
                request.trainingDate(),
                request.trainingDuration());
    }

    private void sendTrainerWorkloadFallback(TrainerWorkloadRequest request, Throwable exception) {
        log.warn("Trainer workload service is unavailable. Workload event was not sent. " +
                        "trainerUsername={}, actionType={}, trainingDate={}, duration={}, reason={}",
                request.trainerUsername(),
                request.actionType(),
                request.trainingDate(),
                request.trainingDuration(),
                exception.getMessage());
    }
}