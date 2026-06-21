package ua.ivan.epam.gym.workload.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;

import java.util.List;
import java.util.Optional;

public interface TrainerWorkloadRepository extends MongoRepository<TrainerWorkload, String> {

    Optional<TrainerWorkload> findByTrainerUsername(String trainerUsername);

    boolean existsByTrainerUsername(String trainerUsername);

    List<TrainerWorkload> findByTrainerFirstNameAndTrainerLastName(
            String trainerFirstName,
            String trainerLastName
    );
}