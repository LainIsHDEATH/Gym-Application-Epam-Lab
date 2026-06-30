package ua.ivan.epam.gym.workload.bdd;

import io.cucumber.java.Before;
import lombok.RequiredArgsConstructor;
import ua.ivan.epam.gym.workload.repository.TrainerWorkloadRepository;

@RequiredArgsConstructor
public class WorkloadHooks {

    private final TrainerWorkloadRepository repository;

    @Before
    public void cleanMongoDatabase() {
        repository.deleteAll();
    }
}