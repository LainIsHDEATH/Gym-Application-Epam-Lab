package ua.ivan.epam.gym.application.actuator.health;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Health health() {
        try {
            Long result = em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                    .getSingleResult();

            return Health.up()
                    .withDetail("database", "available")
                    .withDetail("usersCount", result)
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("database", "unavailable")
                    .build();
        }
    }
}
