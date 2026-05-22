package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.request.ChangeActiveStatusRequest;
import ua.ivan.epam.gym.application.dto.request.RegisterTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.dto.response.TraineeProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.mapper.TraineeMapper;
import ua.ivan.epam.gym.application.mapper.TrainerMapper;
import ua.ivan.epam.gym.application.mapper.UserMapper;
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
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    private final UserMapper userMapper;
    private final TraineeMapper traineeMapper;
    private final TrainerMapper trainerMapper;

    @Transactional
    public RegistrationResponse register(RegisterTraineeProfileRequest request) {
        log.info("Creating trainee profile for {} {}", request.firstName(), request.lastName());

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

        return userMapper.toRegistrationResponse(savedTrainee.getUser());
    }

    @Transactional(readOnly = true)
    public TraineeProfileResponse getProfileByUsername(String username) {
        log.debug("Searching trainee profile by username={}", username);

        return traineeRepository.findProfileByUsername(username)
                .map(traineeMapper::toTraineeProfileResponse)
                .orElseThrow(() -> {
                    log.warn("Trainee not found. username={}", username);
                    return new EntityNotFoundException("Trainee not found");
                });
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
    public TraineeProfileResponse update(UpdateTraineeProfileRequest request) {
        log.info("Updating trainee profile. trainee username={}", request.username());

        Trainee trainee = traineeRepository.findByUsername(request.username())
                .map(t -> {
                    Optional.ofNullable(request.dateOfBirth()).ifPresent(t::setDateOfBirth);
                    Optional.ofNullable(request.address()).ifPresent(t::setAddress);
                    Optional.ofNullable(request.firstName()).ifPresent(t.getUser()::setFirstName);
                    Optional.ofNullable(request.lastName()).ifPresent(t.getUser()::setLastName);
                    Optional.ofNullable(request.isActive()).ifPresent(t.getUser()::setIsActive);
                    return t;
                })
                .orElseThrow(() -> {
                    log.warn("Cannot update trainee. Trainee not found. username={}", request.username());
                    return new EntityNotFoundException("Trainee not found");
                });

        log.info("Updated trainee profile. traineeId={}", trainee.getId());

        return traineeMapper.toTraineeProfileResponse(trainee);
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
    public void changeActiveStatus(ChangeActiveStatusRequest request) {
        log.info("Changing trainee profile status. trainee username={}", request.username());

        Trainee trainee = traineeRepository.findByUsername(request.username())
                .orElseThrow(() -> {
                    log.warn("Cannot change trainee status. Trainee not found. username={}", request.username());
                    return new EntityNotFoundException("Trainee not found. username=" + request.username());
                });

        User user = trainee.getUser();

        user.setIsActive(request.isActive());

        log.info("Changed trainee profile status. traineeId={}, newStatus={}",
                trainee.getId(), user.getIsActive());
    }

    @Transactional
    public List<TrainerShortResponse> updateTrainersList(UpdateTraineeTrainersRequest request) {
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

        return trainee.getTrainers()
                .stream()
                .map(trainerMapper::toTrainerShortResponse)
                .toList();
    }
}