package ua.ivan.epam.gym.application.actuator.metrics;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class GymMetricsAspect {

    private final GymMetrics gymMetrics;

    @Around("@annotation(countGymEvent)")
    public Object countGymEvent(ProceedingJoinPoint joinPoint,
                                CountGymEvent countGymEvent) throws Throwable {
        if (countGymEvent.onlyOnSuccess()) {
            Object result = joinPoint.proceed();
            gymMetrics.increment(countGymEvent.value());
            return result;
        }

        try {
            return joinPoint.proceed();
        } finally {
            gymMetrics.increment(countGymEvent.value());
        }
    }
}
