package ua.ivan.epam.gym.application.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.authentication.AuthService;
import ua.ivan.epam.gym.application.dto.ChangePasswordRequest;
import ua.ivan.epam.gym.application.dto.CreateTraineeRequest;
import ua.ivan.epam.gym.application.dto.CreateTrainerRequest;
import ua.ivan.epam.gym.application.dto.CreateTrainingRequest;
import ua.ivan.epam.gym.application.dto.GetTraineeTrainingsRequest;
import ua.ivan.epam.gym.application.dto.GetTrainerTrainingsRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.dto.UpdateTrainerRequest;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.service.TraineeService;
import ua.ivan.epam.gym.application.service.TrainerService;
import ua.ivan.epam.gym.application.service.TrainingService;
import ua.ivan.epam.gym.application.validation.ValidationService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final AuthService authService;
    private final ValidationService validationService;

    public Trainer createTrainer(CreateTrainerRequest request) {
        validationService.validate(request);
        return trainerService.create(request);
    }

    public Trainee createTrainee(CreateTraineeRequest request) {
        validationService.validate(request);
        return traineeService.create(request);
    }

    public Trainer getTrainerByUsername(String trainerUsername,
                                        String username,
                                        String password) {
        authService.authenticate(username, password);
        return trainerService.getByUsername(trainerUsername);
    }

    public Trainee getTraineeByUsername(String traineeUsername,
                                        String username,
                                        String password) {
        authService.authenticate(username, password);
        return traineeService.getByUsername(traineeUsername);
    }

    public void changeTraineePassword(ChangePasswordRequest request) {
        validationService.validate(request);
        authService.authenticate(request.username(), request.oldPassword());
        traineeService.changePassword(
                request.username(),
                request.oldPassword(),
                request.newPassword()
        );
    }

    public void changeTrainerPassword(ChangePasswordRequest request) {
        validationService.validate(request);
        authService.authenticate(request.username(), request.oldPassword());
        trainerService.changePassword(
                request.username(),
                request.oldPassword(),
                request.newPassword()
        );
    }

    public Trainer updateTrainer(UpdateTrainerRequest request,
                                 String username,
                                 String password) {
        validationService.validate(request);

        authService.authenticate(username, password);

        return trainerService.update(request);
    }

    public Trainee updateTrainee(UpdateTraineeRequest request,
                                 String username,
                                 String password) {
        validationService.validate(request);

        authService.authenticate(username, password);

        return traineeService.update(request);
    }

    public Trainee changeTraineeStatus(Long traineeId,
                                       String username,
                                       String password) {
        authService.authenticate(username, password);
        return traineeService.changeActiveStatus(traineeId);
    }

    public Trainer changeTrainerStatus(Long trainerId,
                                       String username,
                                       String password) {
        authService.authenticate(username, password);
        return trainerService.changeActiveStatus(trainerId);
    }

    public void deleteTraineeByUsername(String traineeUsername,
                                        String username,
                                        String password) {
        authService.authenticate(username, password);
        traineeService.deleteByUsername(traineeUsername);
    }

    public List<Training> getTraineeTrainings(GetTraineeTrainingsRequest request,
                                              String username,
                                              String password) {
        validationService.validate(request);
        authService.authenticate(username, password);
        return trainingService.getTraineeTrainings(request);
    }

    public List<Training> getTrainerTrainings(GetTrainerTrainingsRequest request,
                                              String username,
                                              String password) {
        validationService.validate(request);
        authService.authenticate(username, password);
        return trainingService.getTrainerTrainings(request);
    }

    public Training addTraining(CreateTrainingRequest request,
                                String username,
                                String password) {
        validationService.validate(request);
        authService.authenticate(username, password);
        return trainingService.create(request);
    }

    public List<Trainer> getTrainersNotAssignedToTrainee(String traineeUsername,
                                                         String username,
                                                         String password) {
        authService.authenticate(username, password);
        return trainerService.getNotAssignedToTrainee(traineeUsername);
    }

    public Trainee updateTraineeTrainersList(UpdateTraineeTrainersRequest request,
                                             String username,
                                             String password) {
        validationService.validate(request);
        authService.authenticate(username, password);
        return traineeService.updateTrainersList(request);
    }
}