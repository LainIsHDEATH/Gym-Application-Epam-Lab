package ua.ivan.epam.gym.workload.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.exception.exceptions.InvalidWorkloadMessageException;
import ua.ivan.epam.gym.workload.service.TrainerWorkloadService;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadMessageConsumer {

    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final TrainerWorkloadService trainerWorkloadService;

    @JmsListener(
            destination = "${messaging.queues.trainer-workload}",
            concurrency = "${messaging.listener.trainer-workload.concurrency:1-3}"
    )
    public void consume(
            String payload,
            @Header(name = "X-Transaction-Id", required = false) String transactionId
    ) {
        try {
            putTransactionIdToMdc(transactionId);

            TrainerWorkloadRequest request = deserialize(payload);
            validate(request);

            log.info("Trainer workload message received. trainerUsername={}, actionType={}, trainingDate={}, duration={}",
                    request.trainerUsername(),
                    request.actionType(),
                    request.trainingDate(),
                    request.trainingDuration());

            trainerWorkloadService.updateTrainerWorkload(request);
        } finally {
            MDC.remove(TRANSACTION_ID_MDC_KEY);
        }
    }

    private TrainerWorkloadRequest deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, TrainerWorkloadRequest.class);
        } catch (JsonProcessingException exception) {
            log.warn("Failed to deserialize trainer workload message. payload={}", payload);
            throw new InvalidWorkloadMessageException("Invalid trainer workload message format", exception);
        }
    }

    private void validate(TrainerWorkloadRequest request) {
        Set<ConstraintViolation<TrainerWorkloadRequest>> violations = validator.validate(request);

        if (!violations.isEmpty()) {
            log.warn("Invalid trainer workload message received. violations={}", violations);
            throw new InvalidWorkloadMessageException("Invalid trainer workload message");
        }
    }

    private void putTransactionIdToMdc(String transactionId) {
        if (transactionId != null && !transactionId.isBlank()) {
            MDC.put(TRANSACTION_ID_MDC_KEY, transactionId);
        }
    }
}