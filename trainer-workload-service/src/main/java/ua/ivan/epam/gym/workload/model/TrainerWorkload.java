package ua.ivan.epam.gym.workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class TrainerWorkload {

    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private Boolean isActive;

    @Builder.Default
    private Map<Integer, Map<Integer, Integer>> years = new ConcurrentHashMap<>();

    public void addDuration(int year, int month, int duration) {
        years.computeIfAbsent(year, ignored -> new ConcurrentHashMap<>())
                .merge(month, duration, Integer::sum);
    }

    public void subtractDuration(int year, int month, int duration) {
        years.computeIfAbsent(year, ignored -> new ConcurrentHashMap<>())
                .compute(month, (ignored, currentDuration) -> {
                    int current = currentDuration == null ? 0 : currentDuration;
                    int updated = current - duration;

                    return Math.max(updated, 0);

                });
    }

    public int getDuration(int year, int month) {
        return years.getOrDefault(year, Map.of())
                .getOrDefault(month, 0);
    }
}