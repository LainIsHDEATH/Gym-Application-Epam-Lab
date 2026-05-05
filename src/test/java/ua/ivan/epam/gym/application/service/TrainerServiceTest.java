package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.CreateTrainerRequest;
import ua.ivan.epam.gym.application.dto.UpdateTrainerRequest;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.TraineeRepository;
import ua.ivan.epam.gym.application.repository.TrainerRepository;
import ua.ivan.epam.gym.application.repository.TrainingTypeRepository;
import ua.ivan.epam.gym.application.repository.UserRepository;
import ua.ivan.epam.gym.application.utils.PasswordGenerator;
import ua.ivan.epam.gym.application.utils.UsernameGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void createShouldCreateUserAndTrainerProfile() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                "Mike",
                "Brown",
                1L
        );

        TrainingType specialization = createTrainingType(1L, "Fitness");

        when(trainingTypeRepository.findById(1L))
                .thenReturn(Optional.of(specialization));

        when(userRepository.findAll()).thenReturn(List.of());

        when(usernameGenerator.generate(
                eq("Mike"),
                eq("Brown"),
                any()
        )).thenReturn("Mike.Brown");

        when(passwordGenerator.generate())
                .thenReturn("password12");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer trainer = invocation.getArgument(0);
            trainer.setId(20L);
            return trainer;
        });

        Trainer result = trainerService.create(request);

        assertEquals(20L, result.getId());

        assertNotNull(result.getUser());
        assertEquals(10L, result.getUser().getId());
        assertEquals("Mike", result.getUser().getFirstName());
        assertEquals("Brown", result.getUser().getLastName());
        assertEquals("Mike.Brown", result.getUser().getUsername());
        assertEquals("password12", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());

        assertNotNull(result.getSpecialization());
        assertSame(specialization, result.getSpecialization());
        assertEquals(1L, result.getSpecialization().getId());
        assertEquals("Fitness", result.getSpecialization().getTrainingTypeName());

        verify(trainingTypeRepository).findById(1L);
        verify(userRepository).findAll();
        verify(usernameGenerator).generate(eq("Mike"), eq("Brown"), any());
        verify(passwordGenerator).generate();
        verify(userRepository).save(any(User.class));
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void createShouldPassUsernameExistsPredicateToUsernameGenerator() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                "Mike",
                "Brown",
                1L
        );

        TrainingType specialization = createTrainingType(1L, "Fitness");

        when(trainingTypeRepository.findById(1L))
                .thenReturn(Optional.of(specialization));

        when(userRepository.findAll()).thenReturn(List.of());

        when(usernameGenerator.generate(
                eq("Mike"),
                eq("Brown"),
                any()
        )).thenAnswer(invocation -> {
            Predicate<String> predicate = invocation.getArgument(2);

            when(userRepository.existsByUsername("Mike.Brown"))
                    .thenReturn(true);

            assertTrue(predicate.test("Mike.Brown"));

            return "Mike.Brown1";
        });

        when(passwordGenerator.generate())
                .thenReturn("password12");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(10L);
            return user;
        });

        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer trainer = invocation.getArgument(0);
            trainer.setId(20L);
            return trainer;
        });

        Trainer result = trainerService.create(request);

        assertEquals("Mike.Brown1", result.getUser().getUsername());

        verify(userRepository).existsByUsername("Mike.Brown");
    }

    @Test
    void createShouldThrowExceptionWhenTrainingTypeDoesNotExist() {
        CreateTrainerRequest request = new CreateTrainerRequest(
                "Mike",
                "Brown",
                99L
        );

        when(trainingTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.create(request)
        );

        assertEquals("Training type not found. id=99", exception.getMessage());

        verify(trainingTypeRepository).findById(99L);
        verify(userRepository, never()).save(any());
        verify(trainerRepository, never()).save(any());
        verify(usernameGenerator, never()).generate(anyString(), anyString(), any());
        verify(passwordGenerator, never()).generate();
    }

    @Test
    void getShouldReturnTrainerWhenExists() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");

        when(trainerRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        Trainer result = trainerService.get(1L);

        assertSame(trainer, result);
        assertEquals(1L, result.getId());

        verify(trainerRepository).findById(1L);
    }

    @Test
    void getShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.get(99L)
        );

        assertEquals("Trainer not found", exception.getMessage());

        verify(trainerRepository).findById(99L);
    }

    @Test
    void getByUsernameShouldReturnTrainerWhenExists() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getByUsername("Mike.Brown");

        assertSame(trainer, result);
        assertEquals("Mike.Brown", result.getUser().getUsername());

        verify(trainerRepository).findByUsername("Mike.Brown");
    }

    @Test
    void getByUsernameShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.getByUsername("Unknown.User")
        );

        assertEquals("Trainer not found. username=Unknown.User", exception.getMessage());

        verify(trainerRepository).findByUsername("Unknown.User");
    }

    @Test
    void updateShouldChangeSpecializationWhenTrainerAndTrainingTypeExist() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        TrainingType newSpecialization = createTrainingType(2L, "Yoga");

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                1L,
                2L
        );

        when(trainerRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(trainingTypeRepository.findById(2L))
                .thenReturn(Optional.of(newSpecialization));

        Trainer result = trainerService.update(request);

        assertSame(trainer, result);
        assertSame(newSpecialization, result.getSpecialization());
        assertEquals(2L, result.getSpecialization().getId());
        assertEquals("Yoga", result.getSpecialization().getTrainingTypeName());

        verify(trainerRepository).findById(1L);
        verify(trainingTypeRepository).findById(2L);
        verify(trainerRepository, never()).update(any());
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainerDoesNotExist() {
        UpdateTrainerRequest request = new UpdateTrainerRequest(
                99L,
                1L
        );

        when(trainerRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.update(request)
        );

        assertEquals("Trainer not found", exception.getMessage());

        verify(trainerRepository).findById(99L);
        verify(trainingTypeRepository, never()).findById(anyLong());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainingTypeDoesNotExist() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");

        UpdateTrainerRequest request = new UpdateTrainerRequest(
                1L,
                99L
        );

        when(trainerRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        when(trainingTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.update(request)
        );

        assertEquals("Training type not found. id=99", exception.getMessage());
        assertEquals(1L, trainer.getSpecialization().getId());
        assertEquals("Fitness", trainer.getSpecialization().getTrainingTypeName());

        verify(trainerRepository).findById(1L);
        verify(trainingTypeRepository).findById(99L);
    }

    @Test
    void changeActiveStatusShouldChangeTrueToFalse() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        trainer.getUser().setIsActive(true);

        when(trainerRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        Trainer result = trainerService.changeActiveStatus(1L);

        assertSame(trainer, result);
        assertFalse(result.getUser().getIsActive());

        verify(trainerRepository).findById(1L);
    }

    @Test
    void changeActiveStatusShouldChangeFalseToTrue() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        trainer.getUser().setIsActive(false);

        when(trainerRepository.findById(1L))
                .thenReturn(Optional.of(trainer));

        Trainer result = trainerService.changeActiveStatus(1L);

        assertSame(trainer, result);
        assertTrue(result.getUser().getIsActive());

        verify(trainerRepository).findById(1L);
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.changeActiveStatus(99L)
        );

        assertEquals("Trainer not found. id=99", exception.getMessage());

        verify(trainerRepository).findById(99L);
    }

    @Test
    void changePasswordShouldChangePasswordWhenOldPasswordIsCorrect() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        trainer.getUser().setPassword("oldPassword");

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        trainerService.changePassword(
                "Mike.Brown",
                "oldPassword",
                "newPassword"
        );

        assertEquals("newPassword", trainer.getUser().getPassword());

        verify(trainerRepository).findByUsername("Mike.Brown");
    }

    @Test
    void changePasswordShouldThrowExceptionWhenOldPasswordIsIncorrect() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        trainer.getUser().setPassword("oldPassword");

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainerService.changePassword(
                        "Mike.Brown",
                        "wrongPassword",
                        "newPassword"
                )
        );

        assertEquals("Old password is incorrect", exception.getMessage());
        assertEquals("oldPassword", trainer.getUser().getPassword());

        verify(trainerRepository).findByUsername("Mike.Brown");
    }

    @Test
    void changePasswordShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.changePassword(
                        "Unknown.User",
                        "oldPassword",
                        "newPassword"
                )
        );

        assertEquals("Trainer not found. username=Unknown.User", exception.getMessage());

        verify(trainerRepository).findByUsername("Unknown.User");
    }

    @Test
    void getNotAssignedToTraineeShouldReturnTrainersWhenTraineeExists() {
        Trainee trainee = createTrainee(1L, "John.Smith");

        Trainer trainer1 = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        Trainer trainer2 = createTrainer(2L, "Alice.White", 2L, "Yoga");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findNotAssignedToTrainee("John.Smith"))
                .thenReturn(List.of(trainer1, trainer2));

        List<Trainer> result = trainerService.getNotAssignedToTrainee("John.Smith");

        assertEquals(2, result.size());
        assertSame(trainer1, result.get(0));
        assertSame(trainer2, result.get(1));

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findNotAssignedToTrainee("John.Smith");
    }

    @Test
    void getNotAssignedToTraineeShouldReturnEmptyListWhenNoTrainersFound() {
        Trainee trainee = createTrainee(1L, "John.Smith");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findNotAssignedToTrainee("John.Smith"))
                .thenReturn(List.of());

        List<Trainer> result = trainerService.getNotAssignedToTrainee("John.Smith");

        assertTrue(result.isEmpty());

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findNotAssignedToTrainee("John.Smith");
    }

    @Test
    void getNotAssignedToTraineeShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("Unknown.Trainee"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.getNotAssignedToTrainee("Unknown.Trainee")
        );

        assertEquals("Trainee not found. username=Unknown.Trainee", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.Trainee");
        verify(trainerRepository, never()).findNotAssignedToTrainee(anyString());
    }

    private Trainer createTrainer(Long id,
                                  String username,
                                  Long specializationId,
                                  String specializationName) {
        Trainer trainer = Trainer.builder()
                .id(id)
                .user(createUser(id + 100, username))
                .specialization(createTrainingType(specializationId, specializationName))
                .build();

        trainer.getUser().setTrainer(trainer);

        return trainer;
    }

    private Trainee createTrainee(Long id, String username) {
        Trainee trainee = Trainee.builder()
                .id(id)
                .user(createUser(id + 200, username))
                .dateOfBirth(LocalDate.of(2000, 5, 10))
                .address("London")
                .build();

        trainee.getUser().setTrainee(trainee);

        return trainee;
    }

    private User createUser(Long id, String username) {
        return User.builder()
                .id(id)
                .firstName(username.split("\\.")[0])
                .lastName(username.split("\\.")[1])
                .username(username)
                .password("password12")
                .isActive(true)
                .build();
    }

    private TrainingType createTrainingType(Long id, String name) {
        return TrainingType.builder()
                .id(id)
                .trainingTypeName(name)
                .build();
    }
}