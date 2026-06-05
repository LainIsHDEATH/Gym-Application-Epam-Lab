package ua.ivan.epam.gym.workload.repository;

import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class TrainerWorkloadRepository {

    private final ConcurrentMap<String, TrainerWorkload> storage = new ConcurrentHashMap<>();

    public TrainerWorkload save(TrainerWorkload workload) {
        storage.put(workload.getTrainerUsername(), workload);
        return workload;
    }

    public Optional<TrainerWorkload> findByUsername(String username) {
        return Optional.ofNullable(storage.get(username));
    }

    public TrainerWorkload getOrCreate(String username, TrainerWorkload initialValue) {
        return storage.computeIfAbsent(username, ignored -> initialValue);
    }

    public boolean existsByUsername(String username) {
        return storage.containsKey(username);
    }
}