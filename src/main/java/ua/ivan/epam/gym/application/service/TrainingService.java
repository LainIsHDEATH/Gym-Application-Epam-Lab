package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.request.AddTrainingRequest;
import ua.ivan.epam.gym.application.dto.response.TraineeTrainingResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerTrainingResponse;
import ua.ivan.epam.gym.application.mapper.RestResponseMapper;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.repository.*;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    private final RestResponseMapper mapper;

    @Transactional
    public Training create(AddTrainingRequest request) {

        log.info("Creating training. trainee username={}, trainer username={}, training type name={}, training name={}",
                request.traineeUsername(), request.trainerUsername(), request.trainingTypeName(), request.trainingName());

        Trainee trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Trainee not found. trainee username={}", request.traineeUsername());
                    return new RuntimeException("Trainee not found");
                });

        Trainer trainer = trainerRepository.findByUsername(request.trainerUsername())
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Trainer not found. trainer username={}", request.trainerUsername());
                    return new RuntimeException("Trainer not found");
                });

        TrainingType trainingType = trainingTypeRepository.findByName(request.trainingTypeName())
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Training type not found. training name={}", request.trainingName());
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
    public List<TraineeTrainingResponse> getTraineeTrainings(
            String username,
            LocalDate periodFrom,
            LocalDate periodTo,
            String trainerName,
            String trainingTypeName
    ) {
        return trainingRepository.findTraineeTrainingsByCriteria(
                        username,
                        periodFrom,
                        periodTo,
                        trainerName,
                        trainingTypeName)
                .stream()
                .map(mapper::toTraineeTrainingResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TrainerTrainingResponse> getTrainerTrainings(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        return trainingRepository.findTrainerTrainingsByCriteria(
                trainerUsername,
                fromDate,
                toDate,
                traineeName)
                .stream()
                .map(mapper::toTrainerTrainingResponse)
                .toList();
    }
}