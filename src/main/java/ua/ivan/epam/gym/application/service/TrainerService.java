package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.CreateTrainerRequest;
import ua.ivan.epam.gym.application.dto.UpdateTrainerRequest;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.TraineeRepository;
import ua.ivan.epam.gym.application.repository.TrainerRepository;
import ua.ivan.epam.gym.application.repository.TrainingTypeRepository;
import ua.ivan.epam.gym.application.repository.UserRepository;
import ua.ivan.epam.gym.application.utils.PasswordGenerator;
import ua.ivan.epam.gym.application.utils.UsernameGenerator;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeRepository traineeRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    @Transactional
    public Trainer create(CreateTrainerRequest request) {
        log.info("Creating trainer profile for {} {}, specialization={}",
                request.firstName(), request.lastName(), request.specializationId());

        TrainingType specialization = trainingTypeRepository.findById(request.specializationId())
                .orElseThrow(() -> {
                    log.warn("Cannot create trainer. Training type not found. id={}", request.specializationId());
                    return new EntityNotFoundException("Training type not found. id=" + request.specializationId());
                });

        List<User> users = userRepository.findAll();

        String username = usernameGenerator.generate(request.firstName(), request.lastName(), users);
        String password = passwordGenerator.generate();

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        Trainer trainer = new Trainer();
        trainer.setUser(savedUser);
        trainer.setSpecialization(specialization);

        Trainer savedTrainer = trainerRepository.save(trainer);

        log.info("Created trainer profile. trainerId={}, userId={}, username={}",
                savedTrainer.getId(), savedUser.getId(), savedUser.getUsername());

        return savedTrainer;
    }

    @Transactional(readOnly = true)
    public Trainer get(Long id) {
        log.debug("Searching trainer by id={}", id);

        return trainerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainer not found. id={}", id);
                    return new EntityNotFoundException("Trainer not found");
                });
    }

    @Transactional(readOnly = true)
    public Trainer getByUsername(String username) {
        log.debug("Searching trainer by username={}", username);

        return trainerRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Trainer not found. username=" + username
                ));
    }

    @Transactional
    public Trainer update(UpdateTrainerRequest request) {
        log.info("Updating trainer profile. trainerId={}", request.trainerId());

        Trainer trainer = trainerRepository.findById(request.trainerId())
                .orElseThrow(() -> {
                    log.warn("Cannot update trainer. Trainer not found. trainerId={}", request.trainerId());
                    return new EntityNotFoundException("Trainer not found");
                });

        TrainingType trainingType = trainingTypeRepository.findById(request.specializationId())
                .orElseThrow(() -> {
                    log.warn("Cannot update trainer. Training type not found. id={}", request.specializationId());
                    return new EntityNotFoundException("Training type not found. id=" + request.specializationId());
                });

        trainer.setSpecialization(trainingType);

        log.info("Updated trainer profile. trainerId={}", trainer.getId());

        return trainer;
    }

    @Transactional
    public Trainer changeActiveStatus(Long trainerId) {
        log.info("Changing trainer profile status. trainerId={}", trainerId);

        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> {
                    log.warn("Cannot change trainer status. Trainer not found. id={}", trainerId);
                    return new EntityNotFoundException("Trainer not found. id=" + trainerId);
                });

        User user = trainer.getUser();

        boolean currentStatus = user.getIsActive();

        user.setIsActive(!currentStatus);

        log.info("Changed trainer profile status. trainerId={}, newStatus={}",
                trainer.getId(), user.getIsActive());

        return trainer;
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        Trainer trainer = getByUsername(username);
        User user = trainer.getUser();

        if (!oldPassword.equals(user.getPassword())) {
            log.warn("Cannot change trainer password. Old password is incorrect. username={}", username);
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(newPassword);

        log.info("Changed trainer password. username={}", username);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getNotAssignedToTrainee(String traineeUsername) {
        traineeRepository.findByUsername(traineeUsername)
                .orElseThrow(() -> {
                    log.warn("Trainee not found. username={}", traineeUsername);
                    return new EntityNotFoundException("Trainee not found. username=" + traineeUsername);
                });

        return trainerRepository.findNotAssignedToTrainee(traineeUsername);
    }
}
