package ua.ivan.epam.gym.application.config;

import feign.RequestInterceptor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import ua.ivan.epam.gym.application.security.JwtUtils;

public class TrainerWorkloadFeignConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    private static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Bean
    public RequestInterceptor trainerWorkloadRequestInterceptor(JwtUtils jwtUtils) {
        return template -> {
            String serviceToken = jwtUtils.generateServiceToken();

            template.header(AUTHORIZATION_HEADER, BEARER_PREFIX + serviceToken);

            String transactionId = MDC.get(TRANSACTION_ID_MDC_KEY);
            if (transactionId != null && !transactionId.isBlank()) {
                template.header(TRANSACTION_ID_HEADER, transactionId);
            }
        };
    }
}