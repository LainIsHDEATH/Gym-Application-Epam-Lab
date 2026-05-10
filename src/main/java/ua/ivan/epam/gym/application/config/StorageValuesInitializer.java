package ua.ivan.epam.gym.application.config;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import ua.ivan.epam.gym.application.model.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageValuesInitializer {

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

    @PersistenceContext
    private EntityManager em;

    private final TransactionTemplate transactionTemplate;

    private final Map<Long, User> usersByFileId = new HashMap<>();
    private final Map<Long, Trainer> trainersByFileId = new HashMap<>();
    private final Map<Long, Trainee> traineesByFileId = new HashMap<>();
    private final Map<Long, TrainingType> trainingTypesByFileId = new HashMap<>();

    @PostConstruct
    public void init() {
        transactionTemplate.executeWithoutResult(status -> {
            log.info("Starting initial data loading");

            read(usersPath).forEach(this::parseUser);
            read(trainingTypesPath).forEach(this::parseTrainingType);
            read(trainersPath).forEach(this::parseTrainer);
            read(traineesPath).forEach(this::parseTrainee);
            read(trainingsPath).forEach(this::parseTraining);

            log.info(
                    "Initial data loaded. users={}, trainers={}, trainees={}, trainingTypes={}",
                    usersByFileId.size(),
                    trainersByFileId.size(),
                    traineesByFileId.size(),
                    trainingTypesByFileId.size()
            );
        });
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

        Long fileUserId = Long.parseLong(p[0]);
        String username = p[3];

        User existingUser = findUserByUsername(username);

        if (existingUser != null) {
            usersByFileId.put(fileUserId, existingUser);
            log.debug("User already exists. fileUserId={}, dbUserId={}, username={}",
                    fileUserId, existingUser.getId(), username);
            return;
        }

        User user = new User();
        user.setFirstName(p[1]);
        user.setLastName(p[2]);
        user.setUsername(username);
        user.setPassword(p[4]);
        user.setIsActive(Boolean.parseBoolean(p[5]));

        em.persist(user);
        usersByFileId.put(fileUserId, user);

        log.debug("Parsed user. fileUserId={}, dbUserId={}, username={}, active={}",
                fileUserId, user.getId(), user.getUsername(), user.getIsActive());
    }

    private void parseTrainingType(String line) {
        String[] p = line.split(";");

        Long fileTrainingTypeId = Long.parseLong(p[0]);
        String typeName = p[1];

        TrainingType existingType = findTrainingTypeByName(typeName);

        if (existingType != null) {
            trainingTypesByFileId.put(fileTrainingTypeId, existingType);
            log.debug("Training type already exists. fileTrainingTypeId={}, dbTrainingTypeId={}, name={}",
                    fileTrainingTypeId, existingType.getId(), typeName);
            return;
        }

        TrainingType type = new TrainingType();
        type.setTrainingTypeName(typeName);

        em.persist(type);
        trainingTypesByFileId.put(fileTrainingTypeId, type);

        log.debug("Parsed training type. fileTrainingTypeId={}, dbTrainingTypeId={}, name={}",
                fileTrainingTypeId, type.getId(), type.getTrainingTypeName());
    }

    private void parseTrainer(String line) {
        String[] p = line.split(";");

        Long fileTrainerId = Long.parseLong(p[0]);
        Long fileUserId = Long.parseLong(p[1]);
        Long fileTrainingTypeId = Long.parseLong(p[2]);

        User user = usersByFileId.get(fileUserId);
        if (user == null) {
            throw new IllegalStateException("Trainer references missing userId from file: " + fileUserId);
        }

        TrainingType specialization = trainingTypesByFileId.get(fileTrainingTypeId);
        if (specialization == null) {
            throw new IllegalStateException("Trainer references missing trainingTypeId from file: " + fileTrainingTypeId);
        }

        Trainer existingTrainer = findTrainerByUsername(user.getUsername());

        if (existingTrainer != null) {
            trainersByFileId.put(fileTrainerId, existingTrainer);
            log.debug("Trainer already exists. fileTrainerId={}, dbTrainerId={}, username={}",
                    fileTrainerId, existingTrainer.getId(), user.getUsername());
            return;
        }

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(specialization);

        em.persist(trainer);
        trainersByFileId.put(fileTrainerId, trainer);

        log.debug("Parsed trainer. fileTrainerId={}, dbTrainerId={}, username={}, specialization={}",
                fileTrainerId, trainer.getId(), user.getUsername(), specialization.getTrainingTypeName());
    }

    private void parseTrainee(String line) {
        String[] p = line.split(";");

        Long fileTraineeId = Long.parseLong(p[0]);
        Long fileUserId = Long.parseLong(p[1]);

        User user = usersByFileId.get(fileUserId);
        if (user == null) {
            throw new IllegalStateException("Trainee references missing userId from file: " + fileUserId);
        }

        Trainee existingTrainee = findTraineeByUsername(user.getUsername());

        if (existingTrainee != null) {
            traineesByFileId.put(fileTraineeId, existingTrainee);
            log.debug("Trainee already exists. fileTraineeId={}, dbTraineeId={}, username={}",
                    fileTraineeId, existingTrainee.getId(), user.getUsername());
            return;
        }

        Trainee trainee = new Trainee();
        trainee.setUser(user);
        trainee.setDateOfBirth(parseNullableDate(p[2]));
        trainee.setAddress(parseNullableString(p[3]));

        em.persist(trainee);
        traineesByFileId.put(fileTraineeId, trainee);

        log.debug("Parsed trainee. fileTraineeId={}, dbTraineeId={}, username={}",
                fileTraineeId, trainee.getId(), user.getUsername());
    }

    private void parseTraining(String line) {
        String[] p = line.split(";");

        Long fileTrainingId = Long.parseLong(p[0]);
        Long fileTraineeId = Long.parseLong(p[1]);
        Long fileTrainerId = Long.parseLong(p[2]);
        String trainingName = p[3];
        Long fileTrainingTypeId = Long.parseLong(p[4]);
        LocalDate trainingDate = LocalDate.parse(p[5]);
        Integer trainingDuration = Integer.parseInt(p[6]);

        Trainee trainee = traineesByFileId.get(fileTraineeId);
        if (trainee == null) {
            throw new IllegalStateException("Training references missing traineeId from file: " + fileTraineeId);
        }

        Trainer trainer = trainersByFileId.get(fileTrainerId);
        if (trainer == null) {
            throw new IllegalStateException("Training references missing trainerId from file: " + fileTrainerId);
        }

        TrainingType trainingType = trainingTypesByFileId.get(fileTrainingTypeId);
        if (trainingType == null) {
            throw new IllegalStateException("Training references missing trainingTypeId from file: " + fileTrainingTypeId);
        }

        if (trainingExists(trainee, trainer, trainingName, trainingDate)) {
            log.debug("Training already exists. fileTrainingId={}, name={}, date={}",
                    fileTrainingId, trainingName, trainingDate);
            return;
        }

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingName(trainingName);
        training.setTrainingType(trainingType);
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(trainingDuration);

        trainee.addTrainer(trainer);

        em.persist(training);

        log.debug(
                "Parsed training. fileTrainingId={}, dbTrainingId={}, traineeId={}, trainerId={}, trainingTypeId={}, date={}, duration={}",
                fileTrainingId,
                training.getId(),
                trainee.getId(),
                trainer.getId(),
                trainingType.getId(),
                training.getTrainingDate(),
                training.getTrainingDuration()
        );
    }

    private User findUserByUsername(String username) {
        return em.createQuery("""
                        SELECT u
                        FROM User u
                        WHERE u.username = :username
                        """, User.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Trainer findTrainerByUsername(String username) {
        return em.createQuery("""
                        SELECT t
                        FROM Trainer t
                        JOIN t.user u
                        WHERE u.username = :username
                        """, Trainer.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    private Trainee findTraineeByUsername(String username) {
        return em.createQuery("""
                        SELECT t
                        FROM Trainee t
                        JOIN t.user u
                        WHERE u.username = :username
                        """, Trainee.class)
                .setParameter("username", username)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    private TrainingType findTrainingTypeByName(String name) {
        return em.createQuery("""
                        SELECT t
                        FROM TrainingType t
                        WHERE t.trainingTypeName = :name
                        """, TrainingType.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    private boolean trainingExists(Trainee trainee,
                                   Trainer trainer,
                                   String trainingName,
                                   LocalDate trainingDate) {
        Long count = em.createQuery("""
                        SELECT COUNT(t)
                        FROM Training t
                        WHERE t.trainee = :trainee
                          AND t.trainer = :trainer
                          AND t.trainingName = :trainingName
                          AND t.trainingDate = :trainingDate
                        """, Long.class)
                .setParameter("trainee", trainee)
                .setParameter("trainer", trainer)
                .setParameter("trainingName", trainingName)
                .setParameter("trainingDate", trainingDate)
                .getSingleResult();

        return count > 0;
    }

    private LocalDate parseNullableDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return LocalDate.parse(value);
    }

    private String parseNullableString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value;
    }
}
