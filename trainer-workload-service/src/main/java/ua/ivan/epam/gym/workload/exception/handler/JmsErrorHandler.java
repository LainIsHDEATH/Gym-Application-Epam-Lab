package ua.ivan.epam.gym.workload.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ErrorHandler;

@Component
@Slf4j
public class JmsErrorHandler implements ErrorHandler {

    @Override
    public void handleError(Throwable exception) {
        log.warn(
                "JMS listener failed. Message will be redelivered or moved to DLQ. reason={}",
                exception.getMessage(),
                exception
        );
    }
}