package ua.ivan.epam.gym.application.actuator.metrics;

public enum GymMetric {
    TRAINEE_REGISTRATION("trainee_registration"),
    TRAINER_REGISTRATION("trainer_registration"),
    TRAINING_CREATED("training_created"),
    PASSWORD_CHANGE("password_change"),
    AUTH_FAILURE("auth_failure");

    private final String eventName;

    GymMetric(String eventName) {
        this.eventName = eventName;
    }

    public String eventName() {
        return eventName;
    }
}
