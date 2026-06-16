package ua.ivan.epam.gym.application.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.application.exception.exceptions.WorkloadMessageSerializationException;

@Component
@RequiredArgsConstructor
@Slf4j
public class TrainerWorkloadMessageProducer {

    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${messaging.queues.trainer-workload}")
    private String trainerWorkloadQueue;

    public void send(TrainerWorkloadRequest request) {
        try {
            String payload = objectMapper.writeValueAsString(request);

            jmsTemplate.convertAndSend(
                    trainerWorkloadQueue,
                    payload,
                    this::addHeaders
            );

            log.info("Trainer workload message sent. queue={}, trainerUsername={}, actionType={}, trainingDate={}, duration={}",
                    trainerWorkloadQueue,
                    request.trainerUsername(),
                    request.actionType(),
                    request.trainingDate(),
                    request.trainingDuration());
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize trainer workload message. trainerUsername={}, actionType={}, trainingDate={}, duration={}",
                    request.trainerUsername(),
                    request.actionType(),
                    request.trainingDate(),
                    request.trainingDuration(),
                    exception);
            throw new WorkloadMessageSerializationException("Failed to serialize trainer workload message", exception);
        } catch (JmsException exception) {
            log.error("Failed to send trainer workload message. trainerUsername={}, actionType={}, reason={}",
                    request.trainerUsername(),
                    request.actionType(),
                    exception.getMessage(),
                    exception);
        }
    }

    private Message addHeaders(Message message) throws JMSException {
        String transactionId = MDC.get(TRANSACTION_ID_MDC_KEY);

        if (transactionId != null && !transactionId.isBlank()) {
            message.setStringProperty(TRANSACTION_ID_HEADER, transactionId);
        }

        return message;
    }
}