package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ua.ivan.epam.gym.application.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.application.messaging.TrainerWorkloadMessageProducer;

@Service
@RequiredArgsConstructor
public class TrainerWorkloadIntegrationService {

    private final TrainerWorkloadMessageProducer trainerWorkloadMessageProducer;

    public void sendTrainerWorkload(TrainerWorkloadRequest request) {
        trainerWorkloadMessageProducer.send(request);
    }
}
