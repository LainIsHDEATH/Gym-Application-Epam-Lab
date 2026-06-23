package ua.ivan.epam.gym.workload.repository;

import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;

@Repository
public interface TrainerWorkloadAtomicOperationsRepository {

    void addDuration(
            TrainerWorkloadRequest request,
            int year,
            int month
    );

    void subtractDuration(
            TrainerWorkloadRequest request,
            int year,
            int month
    );
}