package ua.ivan.epam.gym.application.actuator.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GymMetrics {

    private static final String GYM_EVENTS_TOTAL = "gym_events_total";

    private final MeterRegistry meterRegistry;

    public void increment(GymMetric metric) {
        meterRegistry.counter(
                GYM_EVENTS_TOTAL,
                "event", metric.eventName()
        ).increment();
    }
}