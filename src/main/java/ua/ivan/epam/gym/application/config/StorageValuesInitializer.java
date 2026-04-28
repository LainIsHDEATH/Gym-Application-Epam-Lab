package ua.ivan.epam.gym.application.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.dao.*;
import ua.ivan.epam.gym.application.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class StorageValuesInitializer {

    private static final Logger log = LoggerFactory.getLogger(StorageValuesInitializer.class);

    private final Map<Long, User> userStorage;
    private final Map<Long, Trainer> trainerStorage;
    private final Map<Long, Trainee> traineeStorage;
    private final Map<Long, TrainingType> trainingTypeStorage;
    private final Map<Long, Training> trainingStorage;

    private final AtomicLong userIdGenerator;
    private final AtomicLong trainerIdGenerator;
    private final AtomicLong traineeIdGenerator;
    private final AtomicLong trainingTypeIdGenerator;
    private final AtomicLong trainingIdGenerator;

    @Value("${storage.init.users.path}")
    private String usersPath;

    @Value("${storage.init.trainers.path}")
    private String trainersPath;

    @Value("${storage.init.trainees.path}")
    private String traineesPath;

    @Value("${storage.init.training-types.path}")
    private String trainingTypesPath;

    @Value("${storage.init.trainings.path}")
    private String trainingsPath;

    public StorageValuesInitializer(
            Map<Long, User> userStorage,
            Map<Long, Trainer> trainerStorage,
            Map<Long, Trainee> traineeStorage,
            Map<Long, TrainingType> trainingTypeStorage,
            Map<Long, Training> trainingStorage,
            @Qualifier("userIdGenerator")
            AtomicLong userIdGenerator,
            @Qualifier("trainerIdGenerator")
            AtomicLong trainerIdGenerator,
            @Qualifier("traineeIdGenerator")
            AtomicLong traineeIdGenerator,
            @Qualifier("trainingTypeIdGenerator")
            AtomicLong trainingTypeIdGenerator,
            @Qualifier("trainingIdGenerator")
            AtomicLong trainingIdGenerator
    ) {
        this.userStorage = userStorage;
        this.trainerStorage = trainerStorage;
        this.traineeStorage = traineeStorage;
        this.trainingTypeStorage = trainingTypeStorage;
        this.trainingStorage = trainingStorage;
        this.userIdGenerator = userIdGenerator;
        this.trainerIdGenerator = trainerIdGenerator;
        this.traineeIdGenerator = traineeIdGenerator;
        this.trainingTypeIdGenerator = trainingTypeIdGenerator;
        this.trainingIdGenerator = trainingIdGenerator;
    }

    @PostConstruct
    public void init() {
        log.info("Starting initial data loading");

        read(usersPath).forEach(this::parseUser);
        read(trainingTypesPath).forEach(this::parseTrainingType);
        read(trainersPath).forEach(this::parseTrainer);
        read(traineesPath).forEach(this::parseTrainee);
        read(trainingsPath).forEach(this::parseTraining);

        log.info("Initial data loaded. users={}, trainers={}, trainees={}, trainingTypes={}, trainings={}",
                userStorage.size(),
                trainerStorage.size(),
                traineeStorage.size(),
                trainingTypeStorage.size(),
                trainingStorage.size());

        syncIdGenerators();
    }

    private List<String> read(String path) {
        log.debug("Reading initial data file: {}", path);

        try {
            ClassPathResource resource = new ClassPathResource(path);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
            )) {
                List<String> lines = reader.lines()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .filter(line -> !line.startsWith("#"))
                        .toList();

                log.info("Loaded {} lines from file {}", lines.size(), path);

                return lines;
            }
        } catch (Exception e) {
            log.error("Failed to load initial data file: {}", path, e);
            throw new IllegalStateException("Failed to load initial data file: " + path, e);
        }
    }

    private void parseUser(String line) {
        String[] p = line.split(";");

        User user = new User();
        user.setId(Long.parseLong(p[0]));
        user.setFirstName(p[1]);
        user.setLastName(p[2]);
        user.setUsername(p[3]);
        user.setPassword(p[4]);
        user.setIsActive(Boolean.parseBoolean(p[5]));

        userStorage.put(user.getId(), user);

        log.debug(
                "Parsed user. userId={}, username={}, active={}",
                user.getId(),
                user.getUsername(),
                user.getIsActive()
        );
    }

    private void parseTrainer(String line) {
        String[] p = line.split(";");

        Long trainerId = Long.parseLong(p[0]);
        Long userId = Long.parseLong(p[1]);

        if (!userStorage.containsKey(userId)) {
            log.error("Trainer references missing user. trainerId={}, userId={}", trainerId, userId);
            throw new IllegalStateException("Trainer references missing userId: " + userId);
        }

        Trainer trainer = new Trainer();
        trainer.setId(trainerId);
        trainer.setUserId(userId);
        trainer.setSpecialization(p[2]);

        trainerStorage.put(trainer.getId(), trainer);

        log.debug(
                "Parsed trainer. trainerId={}, userId={}, specialization={}",
                trainer.getId(),
                trainer.getUserId(),
                trainer.getSpecialization()
        );
    }

    private void parseTrainee(String line) {
        String[] p = line.split(";");

        Long traineeId = Long.parseLong(p[0]);
        Long userId = Long.parseLong(p[1]);

        if (!userStorage.containsKey(userId)) {
            log.error("Trainee references missing user. traineeId={}, userId={}", traineeId, userId);
            throw new IllegalStateException("Trainee references missing userId: " + userId);
        }

        Trainee trainee = new Trainee();
        trainee.setId(traineeId);
        trainee.setUserId(userId);
        trainee.setDateOfBirth(LocalDate.parse(p[2]));
        trainee.setAddress(p[3]);

        traineeStorage.put(trainee.getId(), trainee);
    }

    private void parseTrainingType(String line) {
        String[] p = line.split(";");

        TrainingType type = new TrainingType();
        type.setId(Long.parseLong(p[0]));
        type.setTrainingTypeName(p[1]);

        trainingTypeStorage.put(type.getId(), type);

        log.debug(
                "Parsed training type. trainingTypeId={}, name={}",
                type.getId(),
                type.getTrainingTypeName()
        );
    }

    private void parseTraining(String line) {
        String[] p = line.split(";");

        Long trainingId = Long.parseLong(p[0]);
        Long traineeId = Long.parseLong(p[1]);
        Long trainerId = Long.parseLong(p[2]);
        Long trainingTypeId = Long.parseLong(p[4]);

        if (!traineeStorage.containsKey(traineeId)) {
            log.error("Training references missing trainee. trainingId={}, traineeId={}", trainingId, traineeId);
            throw new IllegalStateException("Training references missing traineeId: " + traineeId);
        }

        if (!trainerStorage.containsKey(trainerId)) {
            log.error("Training references missing trainer. trainingId={}, trainerId={}", trainingId, trainerId);
            throw new IllegalStateException("Training references missing trainerId: " + trainerId);
        }

        if (!trainingTypeStorage.containsKey(trainingTypeId)) {
            log.error(
                    "Training references missing training type. trainingId={}, trainingTypeId={}",
                    trainingId,
                    trainingTypeId
            );
            throw new IllegalStateException("Training references missing trainingTypeId: " + trainingTypeId);
        }

        Training training = new Training();
        training.setId(trainingId);
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName(p[3]);
        training.setTrainingTypeId(trainingTypeId);
        training.setTrainingDate(LocalDate.parse(p[5]));
        training.setTrainingDuration(Integer.parseInt(p[6]));

        trainingStorage.put(training.getId(), training);

        log.debug(
                "Parsed training. trainingId={}, traineeId={}, trainerId={}, trainingTypeId={}, date={}, duration={}",
                training.getId(),
                training.getTraineeId(),
                training.getTrainerId(),
                training.getTrainingTypeId(),
                training.getTrainingDate(),
                training.getTrainingDuration()
        );
    }

    private void syncIdGenerators() {
        long maxUserId = maxId(userStorage);
        long maxTrainerId = maxId(trainerStorage);
        long maxTraineeId = maxId(traineeStorage);
        long maxTrainingTypeId = maxId(trainingTypeStorage);
        long maxTrainingId = maxId(trainingStorage);

        userIdGenerator.set(maxUserId);
        trainerIdGenerator.set(maxTrainerId);
        traineeIdGenerator.set(maxTraineeId);
        trainingTypeIdGenerator.set(maxTrainingTypeId);
        trainingIdGenerator.set(maxTrainingId);

        log.info(
                "Id generators synchronized. userId={}, trainerId={}, traineeId={}, trainingTypeId={}, trainingId={}",
                maxUserId,
                maxTrainerId,
                maxTraineeId,
                maxTrainingTypeId,
                maxTrainingId
        );
    }

    private long maxId(Map<Long, ?> storage) {
        return storage.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);
    }
}
