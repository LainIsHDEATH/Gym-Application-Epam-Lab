package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.CreateTrainingRequest;
import ua.ivan.epam.gym.application.dto.GetTraineeTrainingsRequest;
import ua.ivan.epam.gym.application.dto.GetTrainerTrainingsRequest;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    @Transactional
    public Training create(CreateTrainingRequest request) {

        log.info("Creating training. traineeId={}, trainerId={}, trainingTypeId={}, name={}",
                request.traineeId(), request.trainerId(), request.trainingTypeId(), request.trainingName());

        Trainee trainee = traineeRepository.findById(request.traineeId())
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Trainee not found. traineeId={}", request.traineeId());
                    return new RuntimeException("Trainee not found");
                });

        Trainer trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Trainer not found. trainerId={}", request.trainerId());
                    return new RuntimeException("Trainer not found");
                });

        TrainingType trainingType = trainingTypeRepository.findById(request.trainingTypeId())
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Training type not found. trainingTypeId={}", request.trainingTypeId());
                    return new RuntimeException("Training type not found");
                });


        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.trainingName())
                .trainingType(trainingType)
                .trainingDate(request.trainingDate())
                .trainingDuration(request.trainingDuration())
                .build();

        trainee.addTrainer(trainer);

        Training savedTraining = trainingRepository.save(training);

        log.info("Created training. trainingId={}, traineeId={}, trainerId={}",
                savedTraining.getId(), savedTraining.getTrainee().getId(), savedTraining.getTrainer().getId());

        return savedTraining;
    }

    @Transactional(readOnly = true)
    public Training get(Long id) {
        log.debug("Searching training by id={}", id);

        return trainingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Training not found. id={}", id);
                    return new RuntimeException("Training not found");
                });
    }

    @Transactional(readOnly = true)
    public List<Training> getAll() {
        List<Training> trainings = trainingRepository.findAll();

        log.debug("Loaded all trainings. count={}", trainings.size());

        return trainings;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(GetTraineeTrainingsRequest request) {
        return trainingRepository.findTraineeTrainingsByCriteria(
                request.traineeUsername(),
                request.fromDate(),
                request.toDate(),
                request.trainerName(),
                request.trainingTypeId()
        );
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(GetTrainerTrainingsRequest request) {
        return trainingRepository.findTrainerTrainingsByCriteria(
                request.trainerUsername(),
                request.fromDate(),
                request.toDate(),
                request.traineeName()
        );
    }
}