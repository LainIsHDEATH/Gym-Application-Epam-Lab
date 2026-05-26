package ua.ivan.epam.gym.application.actuator.metrics;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class GymFailureMetricsAspect {

    private final GymMetrics gymMetrics;

    @Around("@annotation(countGymFailure)")
    public Object countGymFailure(ProceedingJoinPoint joinPoint,
                                  CountGymFailure countGymFailure) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Throwable exception) {
            if (shouldCount(exception, countGymFailure.exceptions())) {
                gymMetrics.increment(countGymFailure.value());
            }

            throw exception;
        }
    }

    private boolean shouldCount(Throwable exception,
                                Class<? extends Throwable>[] expectedExceptions) {
        if (expectedExceptions.length == 0) {
            return true;
        }

        return Arrays.stream(expectedExceptions)
                .anyMatch(expectedException -> expectedException.isAssignableFrom(exception.getClass()));
    }
}