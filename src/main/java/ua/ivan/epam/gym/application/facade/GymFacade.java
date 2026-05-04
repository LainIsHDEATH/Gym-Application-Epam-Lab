package ua.ivan.epam.gym.application.facade;

import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.authentication.AuthService;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.service.TraineeService;
import ua.ivan.epam.gym.application.service.TrainerService;
import ua.ivan.epam.gym.application.service.TrainingService;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final AuthService authService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     AuthService authService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.authService = authService;
    }

    // ========================
    // Trainee operations
    // ========================

    public Trainee createTrainee(String firstName,
                                 String lastName,
                                 LocalDate dateOfBirth,
                                 String address) {
        return traineeService.create(firstName, lastName, dateOfBirth, address);
    }

    public Trainee getTrainee(Long traineeId, String username, String password) {
        authService.authenticate(username, password);
        return traineeService.get(traineeId);
    }

    public Trainee updateTrainee(Trainee trainee, String username, String password) {
        authService.authenticate(username, password);
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long traineeId, String username, String password) {
        authService.authenticate(username, password);
        traineeService.delete(traineeId);
    }

    // ========================
    // Trainer operations
    // ========================

    public Trainer createTrainer(String firstName,
                                 String lastName,
                                 String specialization) {
        return trainerService.create(firstName, lastName, specialization);
    }

    public Trainer getTrainer(Long trainerId, String username, String password) {
        authService.authenticate(username, password);
        return trainerService.get(trainerId);
    }

    public Trainer updateTrainer(Trainer trainer, String username, String password) {
        authService.authenticate(username, password);
        return trainerService.update(trainer);
    }

    // ========================
    // Training operations
    // ========================

    public Training createTraining(Long traineeId,
                                   Long trainerId,
                                   String trainingName,
                                   Long trainingTypeId,
                                   LocalDate trainingDate,
                                   int trainingDuration,
                                   String username,
                                   String password) {
        authService.authenticate(username, password);
        return trainingService.create(
                traineeId,
                trainerId,
                trainingName,
                trainingTypeId,
                trainingDate,
                trainingDuration
        );
    }

    public Training getTraining(Long trainingId, String username, String password) {
        authService.authenticate(username, password);
        return trainingService.get(trainingId);
    }

    public List<Training> getAllTrainings(String username, String password) {
        authService.authenticate(username, password);
        return trainingService.getAll();
    }
}
