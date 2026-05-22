package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.request.ChangeActiveStatusRequest;
import ua.ivan.epam.gym.application.dto.request.RegisterTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.mapper.TrainerMapper;
import ua.ivan.epam.gym.application.mapper.UserMapper;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainerService {

    private final UserRepository userRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TraineeRepository traineeRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    private final UserMapper userMapper;
    private final TrainerMapper trainerMapper;

    @Transactional
    public RegistrationResponse register(RegisterTrainerProfileRequest request) {
        log.info("Creating trainer profile for {} {}, specialization id={}",
                request.firstName(), request.lastName(), request.specializationId());

        TrainingType specialization = trainingTypeRepository.findById(request.specializationId())
                .orElseThrow(() -> {
                    log.warn("Cannot create trainer. Training type not found. id={}", request.specializationId());
                    return new EntityNotFoundException("Training type not found. id=" + request.specializationId());
                });

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

        Trainer trainer = new Trainer();
        trainer.setUser(savedUser);
        trainer.setSpecialization(specialization);

        Trainer savedTrainer = trainerRepository.save(trainer);

        log.info("Created trainer profile. trainerId={}, userId={}, username={}",
                savedTrainer.getId(), savedUser.getId(), savedUser.getUsername());

        return userMapper.toRegistrationResponse(savedTrainer.getUser());
    }

    @Transactional(readOnly = true)
    public TrainerProfileResponse getProfileByUsername(String username) {
        log.debug("Searching trainer profile by username={}", username);

        return trainerRepository.findProfileByUsername(username)
                .map(trainerMapper::toTrainerProfileResponse)
                .orElseThrow(() -> {
                    log.warn("Trainer not found. username={}", username);
                    return new EntityNotFoundException("Trainer not found");
                });
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
                .orElseThrow(() -> {
                    log.warn("Trainer not found. username={}", username);
                    return new EntityNotFoundException("Trainer not found. username=" + username);
                });

    }

    @Transactional
    public TrainerProfileResponse update(UpdateTrainerProfileRequest request) {
        log.info("Updating trainer profile. trainer username={}", request.username());

        Trainer trainer = trainerRepository.findByUsername(request.username())
                .map(t -> {
                    Optional.ofNullable(request.firstName()).ifPresent(t.getUser()::setFirstName);
                    Optional.ofNullable(request.lastName()).ifPresent(t.getUser()::setLastName);
                    Optional.ofNullable(request.isActive()).ifPresent(t.getUser()::setIsActive);
                    return t;
                })
                .orElseThrow(() -> {
                    log.warn("Cannot update trainer. Trainer not found. trainer username={}", request.username());
                    return new EntityNotFoundException("Trainer not found");
                });

        log.info("Updated trainer profile. trainerId={}", trainer.getId());

        return trainerMapper.toTrainerProfileResponse(trainer);
    }

    @Transactional
    public void changeActiveStatus(ChangeActiveStatusRequest request) {
        log.info("Changing trainer profile status. trainer username={}", request.username());

        Trainer trainer = trainerRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Cannot change trainer status. Trainer not found. username={}", request.username());
                    return new EntityNotFoundException("Trainer not found. username=" + request.username());
                });

        User user = trainer.getUser();

        user.setIsActive(request.isActive());

        log.info("Changed trainer profile status. trainerId={}, newStatus={}",
                trainer.getId(), user.getIsActive());
    }

    @Transactional(readOnly = true)
    public List<TrainerShortResponse> getTrainersNotAssignedToTrainee(String traineeUsername) {
        if (!traineeRepository.existsByUsername(traineeUsername)) {
            log.warn("Trainee not found. username={}", traineeUsername);
            throw new EntityNotFoundException("Trainee not found. username=" + traineeUsername);
        }

        return trainerRepository.findNotAssignedToTrainee(traineeUsername).stream()
                .map(trainerMapper::toTrainerShortResponse)
                .toList();
    }
}
