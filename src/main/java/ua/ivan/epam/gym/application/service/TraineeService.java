package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.CreateTraineeRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.TraineeRepository;
import ua.ivan.epam.gym.application.repository.TrainerRepository;
import ua.ivan.epam.gym.application.repository.UserRepository;
import ua.ivan.epam.gym.application.utils.PasswordGenerator;
import ua.ivan.epam.gym.application.utils.UsernameGenerator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;
    private final TrainerRepository trainerRepository;

    @Transactional
    public Trainee create(CreateTraineeRequest request) {
        log.info("Creating trainee profile for {} {}", request.firstName(), request.lastName());

        List<User> users = userRepository.findAll();

        String username = usernameGenerator.generate(
                request.firstName(),
                request.lastName(),
                userRepository::existsByUsername);
        String password = passwordGenerator.generate();

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .username(username)
                .password(password)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);

        Trainee trainee = Trainee.builder()
                .user(savedUser)
                .dateOfBirth(request.dateOfBirth())
                .address(request.address())
                .build();

        Trainee savedTrainee = traineeRepository.save(trainee);

        log.info("Created trainee profile. traineeId={}, userId={}, username={}",
                savedTrainee.getId(), savedUser.getId(), savedUser.getUsername());

        return savedTrainee;
    }

    @Transactional(readOnly = true)
    public Trainee get(Long id) {
        log.debug("Searching trainee by id={}", id);

        return traineeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainee not found. id={}", id);
                    return new EntityNotFoundException("Trainee not found");
                });
    }

    @Transactional(readOnly = true)
    public Trainee getByUsername(String username) {
        log.debug("Searching trainee by username={}", username);

        return traineeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Trainee not found. username={}", username);
                    return new EntityNotFoundException("Trainee not found");
                });
    }

    @Transactional
    public Trainee update(UpdateTraineeRequest request) {
        log.info("Updating trainee profile. traineeId={}", request.traineeId());

        Trainee trainee = traineeRepository.findById(request.traineeId())
                .orElseThrow(() -> {
                    log.warn("Cannot update trainee. Trainee not found. traineeId={}", request.traineeId());
                    return new EntityNotFoundException("Trainee not found");
                });

        if (request.dateOfBirth() != null) {
            trainee.setDateOfBirth(request.dateOfBirth());
        }

        if (request.address() != null) {
            trainee.setAddress(request.address());
        }

        log.info("Updated trainee profile. traineeId={}", trainee.getId());

        return trainee;
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting trainee profile. traineeId={}", id);

        traineeRepository.deleteById(id);

        log.info("Deleted trainee profile. traineeId={}", id);
    }

    @Transactional
    public void deleteByUsername(String username) {
        log.info("Deleting trainee profile. username={}", username);

        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Cannot delete trainee. Trainee not found. username={}", username);
                    return new EntityNotFoundException("Trainee not found");
                });

        traineeRepository.deleteById(trainee.getId());

        log.info("Deleted trainee profile. traineeId={}, username={}", trainee.getId(), username);
    }

    @Transactional
    public Trainee changeActiveStatus(Long traineeId) {
        log.info("Changing trainee profile status. traineeId={}", traineeId);

        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> {
                    log.warn("Cannot change trainee status. Trainee not found. id={}", traineeId);
                    return new EntityNotFoundException("Trainee not found. id=" + traineeId);
                });

        User user = trainee.getUser();

        boolean currentStatus = user.getIsActive();

        user.setIsActive(!currentStatus);

        log.info("Changed trainee profile status. traineeId={}, newStatus={}",
                trainee.getId(), user.getIsActive());

        return trainee;
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        Trainee trainee = getByUsername(username);
        User user = trainee.getUser();

        if (!oldPassword.equals(user.getPassword())) {
            log.warn("Cannot change trainee password. Old password is incorrect. username={}", username);
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(newPassword);

        log.info("Changed trainee password. username={}", username);
    }

    @Transactional
    public Trainee updateTrainersList(UpdateTraineeTrainersRequest request) {
        Trainee trainee = traineeRepository.findByUsername(request.traineeUsername())
                .orElseThrow(() -> {
                    log.warn("Trainee not found. username={}", request.traineeUsername());
                    return new EntityNotFoundException("Trainee not found. username=" + request.traineeUsername());
                });

        List<Trainer> newTrainers = request.trainerUsernames().stream()
                .map(username -> trainerRepository.findByUsername(username)
                        .orElseThrow(() -> {
                            log.warn("Trainer not found. username={}", username);
                            return new EntityNotFoundException("Trainer not found. username=" + username);
                        }))
                .toList();

        Set<Trainer> currentTrainers = new HashSet<>(trainee.getTrainers());

        for (Trainer trainer : currentTrainers) {
            trainee.removeTrainer(trainer);
        }

        for (Trainer trainer : newTrainers) {
            trainee.addTrainer(trainer);
        }

        log.info("Updated trainee trainers list. traineeUsername={}, trainersCount={}",
                request.traineeUsername(), newTrainers.size());

        return trainee;
    }
}