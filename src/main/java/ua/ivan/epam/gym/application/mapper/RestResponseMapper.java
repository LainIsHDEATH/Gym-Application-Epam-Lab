package ua.ivan.epam.gym.application.mapper;

import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerTrainingResponse;
import ua.ivan.epam.gym.application.dto.response.UpdateTraineeTrainersResponse;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.dto.response.*;

import java.util.Comparator;
import java.util.List;

@Component
public class RestResponseMapper {

    public RegistrationResponse toRegistrationResponse(User user) {
        return new RegistrationResponse(
                user.getUsername(),
                user.getPassword()
        );
    }

    public TrainingTypeResponse toTrainingTypeResponse(TrainingType trainingType) {
        return new TrainingTypeResponse(
                trainingType.getId(),
                trainingType.getTrainingTypeName()
        );
    }

    public TrainerShortResponse toTrainerShortResponse(Trainer trainer) {
        User user = trainer.getUser();

        return new TrainerShortResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                toTrainingTypeResponse(trainer.getSpecialization())
        );
    }

    public TraineeShortResponse toTraineeShortResponse(Trainee trainee) {
        User user = trainee.getUser();

        return new TraineeShortResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public TraineeProfileResponse toTraineeProfileResponse(Trainee trainee) {
        User user = trainee.getUser();

        List<TrainerShortResponse> trainers = trainee.getTrainers()
                .stream()
                .sorted(Comparator.comparing(t -> t.getUser().getUsername()))
                .map(this::toTrainerShortResponse)
                .toList();

        return new TraineeProfileResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                user.getIsActive(),
                trainers
        );
    }

    public TrainerProfileResponse toTrainerProfileResponse(Trainer trainer) {
        User user = trainer.getUser();

        List<TraineeShortResponse> trainees = trainer.getTrainees()
                .stream()
                .sorted(Comparator.comparing(t -> t.getUser().getUsername()))
                .map(this::toTraineeShortResponse)
                .toList();

        return new TrainerProfileResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                toTrainingTypeResponse(trainer.getSpecialization()),
                user.getIsActive(),
                trainees
        );
    }

    public TraineeTrainingResponse toTraineeTrainingResponse(Training training) {
        User trainerUser = training.getTrainer().getUser();

        return new TraineeTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                toTrainingTypeResponse(training.getTrainingType()),
                training.getTrainingDuration(),
                trainerUser.getFirstName() + " " + trainerUser.getLastName()
        );
    }

    public TrainerTrainingResponse toTrainerTrainingResponse(Training training) {
        User traineeUser = training.getTrainee().getUser();

        return new TrainerTrainingResponse(
                training.getTrainingName(),
                training.getTrainingDate(),
                toTrainingTypeResponse(training.getTrainingType()),
                training.getTrainingDuration(),
                traineeUser.getFirstName() + " " + traineeUser.getLastName()
        );
    }

    public UpdateTraineeTrainersResponse toUpdateTraineeTrainersResponse(Trainee trainee) {
        List<TrainerShortResponse> trainers = trainee.getTrainers()
                .stream()
                .sorted(Comparator.comparing(t -> t.getUser().getUsername()))
                .map(this::toTrainerShortResponse)
                .toList();

        return new UpdateTraineeTrainersResponse(trainers);
    }
}
