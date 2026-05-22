package ua.ivan.epam.gym.application.actuator.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.repository.TrainingTypeRepository;

@Component("trainingTypes")
@RequiredArgsConstructor
public class TrainingTypesHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    @Override
    public Health health() {
        try {
            int count = trainingTypeRepository.findAll().size();

            if (count == 0) {
                return Health.down()
                        .withDetail("reason", "No training types found")
                        .withDetail("trainingTypesCount", 0)
                        .build();
            }

            return Health.up()
                    .withDetail("trainingTypesCount", count)
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("reason", "Failed to load training types")
                    .build();
        }
    }
}