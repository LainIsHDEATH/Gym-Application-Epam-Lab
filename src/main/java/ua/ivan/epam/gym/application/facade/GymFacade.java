package ua.ivan.epam.gym.application.facade;

import org.springframework.stereotype.Component;
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

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
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

    public Trainee getTrainee(Long traineeId) {
        return traineeService.get(traineeId);
    }

    public Trainee updateTrainee(Trainee trainee) {
        return traineeService.update(trainee);
    }

    public void deleteTrainee(Long traineeId) {
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

    public Trainer getTrainer(Long trainerId) {
        return trainerService.get(trainerId);
    }

    public Trainer updateTrainer(Trainer trainer) {
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
                                   int trainingDuration) {
        return trainingService.create(
                traineeId,
                trainerId,
                trainingName,
                trainingTypeId,
                trainingDate,
                trainingDuration
        );
    }

    public Training getTraining(Long trainingId) {
        return trainingService.get(trainingId);
    }

    public List<Training> getAllTrainings() {
        return trainingService.getAll();
    }
}
