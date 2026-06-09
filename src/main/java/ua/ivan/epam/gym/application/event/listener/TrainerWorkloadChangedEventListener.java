package ua.ivan.epam.gym.application.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import ua.ivan.epam.gym.application.event.TrainerWorkloadChangedEvent;
import ua.ivan.epam.gym.application.service.TrainerWorkloadIntegrationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadChangedEventListener {

    private final TrainerWorkloadIntegrationService trainerWorkloadIntegrationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(TrainerWorkloadChangedEvent event) {
        log.info(
                "Handling trainer workload event after transaction commit. trainerUsername={}, actionType={}",
                event.request().trainerUsername(),
                event.request().actionType()
        );

        trainerWorkloadIntegrationService.sendTrainerWorkload(event.request());
    }
}