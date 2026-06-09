package ua.ivan.epam.gym.application.mapper;

import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.application.dto.request.WorkloadActionType;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.User;

@Component
public class TrainerWorkloadMapper {

    public TrainerWorkloadRequest toRequest(Training training, WorkloadActionType actionType) {
        Trainer trainer = training.getTrainer();
        User trainerUser = trainer.getUser();

        return new TrainerWorkloadRequest(
                trainerUser.getUsername(),
                trainerUser.getFirstName(),
                trainerUser.getLastName(),
                trainerUser.getIsActive(),
                training.getTrainingDate(),
                training.getTrainingDuration(),
                actionType
        );
    }
}