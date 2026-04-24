package ua.ivan.epam.gym.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class IdGeneratorConfig {

    @Bean
    public AtomicLong userIdGenerator() {
        return new AtomicLong(0);
    }

    @Bean
    public AtomicLong trainerIdGenerator() {
        return new AtomicLong(0);
    }

    @Bean
    public AtomicLong traineeIdGenerator() {
        return new AtomicLong(0);
    }

    @Bean
    public AtomicLong trainingIdGenerator() {
        return new AtomicLong(0);
    }

    @Bean
    public AtomicLong trainingTypeIdGenerator() {
        return new AtomicLong(0);
    }
}
