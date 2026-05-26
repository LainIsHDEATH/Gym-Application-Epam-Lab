package ua.ivan.epam.gym.application.actuator.metrics;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CountGymFailure {
    GymMetric value();

    Class<? extends Throwable>[] exceptions() default {};
}