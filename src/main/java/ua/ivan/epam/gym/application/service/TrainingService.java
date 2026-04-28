package ua.ivan.epam.gym.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.ivan.epam.gym.application.dao.*;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingService.class);

    private final CrudDao<Long, Training> trainingDao;
    private final CrudDao<Long, Trainee> traineeDao;
    private final CrudDao<Long, Trainer> trainerDao;
    private final CrudDao<Long, TrainingType> trainingTypeDao;

    @Autowired
    public TrainingService(CrudDao<Long, Training> trainingDao,
                           CrudDao<Long, Trainee> traineeDao,
                           CrudDao<Long, Trainer> trainerDao,
                           CrudDao<Long, TrainingType> trainingTypeDao) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
    }

    public Training create(Long traineeId,
                           Long trainerId,
                           String name,
                           Long trainingTypeId,
                           LocalDate date,
                           int duration) {

        log.info("Creating training. traineeId={}, trainerId={}, trainingTypeId={}, name={}",
                traineeId, trainerId, trainingTypeId, name);

        traineeDao.findById(traineeId)
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Trainee not found. traineeId={}", traineeId);
                    return new RuntimeException("Trainee not found");
                });

        trainerDao.findById(trainerId)
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Trainer not found. trainerId={}", trainerId);
                    return new RuntimeException("Trainer not found");
                });

        trainingTypeDao.findById(trainingTypeId)
                .orElseThrow(() -> {
                    log.warn("Cannot create training. Training type not found. trainingTypeId={}", trainingTypeId);
                    return new RuntimeException("Training type not found");
                });


        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(name);
        training.setTrainingTypeId(trainingTypeId);
        training.setTrainingDate(date);
        training.setTrainingDuration(duration);
        Training savedTraining = trainingDao.save(training);

        log.info("Created training. trainingId={}, traineeId={}, trainerId={}",
                savedTraining.getId(), savedTraining.getTraineeId(), savedTraining.getTrainerId());

        return savedTraining;
    }

    public Training get(Long id) {
        log.debug("Searching training by id={}", id);

        return trainingDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Training not found. id={}", id);
                    return new RuntimeException("Training not found");
                });
    }

    public List<Training> getAll() {
        List<Training> trainings = trainingDao.findAll();

        log.debug("Loaded all trainings. count={}", trainings.size());

        return trainings;
    }
}