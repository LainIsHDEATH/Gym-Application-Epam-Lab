package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.CreateTraineeRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.TraineeRepository;
import ua.ivan.epam.gym.application.repository.TrainerRepository;
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
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void createShouldCreateUserAndTraineeProfile() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        when(userRepository.findAll()).thenReturn(List.of());

        when(usernameGenerator.generate(
                eq("John"),
                eq("Smith"),
                any()
        )).thenReturn("John.Smith");

        when(passwordGenerator.generate()).thenReturn("password12");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            trainee.setId(10L);
            return trainee;
        });

        Trainee result = traineeService.create(request);

        assertEquals(10L, result.getId());
        assertEquals(LocalDate.of(2000, 5, 10), result.getDateOfBirth());
        assertEquals("London", result.getAddress());

        assertNotNull(result.getUser());
        assertEquals(1L, result.getUser().getId());
        assertEquals("John", result.getUser().getFirstName());
        assertEquals("Smith", result.getUser().getLastName());
        assertEquals("John.Smith", result.getUser().getUsername());
        assertEquals("password12", result.getUser().getPassword());
        assertTrue(result.getUser().getIsActive());

        verify(userRepository).findAll();
        verify(usernameGenerator).generate(eq("John"), eq("Smith"), any());
        verify(passwordGenerator).generate();
        verify(userRepository).save(any(User.class));
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void createShouldPassUsernameExistsPredicateToUsernameGenerator() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        when(userRepository.findAll()).thenReturn(List.of());

        when(usernameGenerator.generate(
                eq("John"),
                eq("Smith"),
                any()
        )).thenAnswer(invocation -> {
            Predicate<String> predicate = invocation.getArgument(2);

            when(userRepository.existsByUsername("John.Smith"))
                    .thenReturn(true);

            assertTrue(predicate.test("John.Smith"));

            return "John.Smith1";
        });

        when(passwordGenerator.generate()).thenReturn("password12");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            trainee.setId(10L);
            return trainee;
        });

        Trainee result = traineeService.create(request);

        assertEquals("John.Smith1", result.getUser().getUsername());

        verify(userRepository).existsByUsername("John.Smith");
    }

    @Test
    void getShouldReturnTraineeWhenExists() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.get(1L);

        assertSame(trainee, result);
        assertEquals(1L, result.getId());

        verify(traineeRepository).findById(1L);
    }

    @Test
    void getShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.get(99L)
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findById(99L);
    }

    @Test
    void getByUsernameShouldReturnTraineeWhenExists() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        Trainee result = traineeService.getByUsername("John.Smith");

        assertSame(trainee, result);
        assertEquals("John.Smith", result.getUser().getUsername());

        verify(traineeRepository).findByUsername("John.Smith");
    }

    @Test
    void getByUsernameShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.getByUsername("Unknown.User")
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.User");
    }

    @Test
    void updateShouldUpdateDateOfBirthAndAddressWhenTraineeExists() {
        Trainee trainee = createTrainee();

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                1L,
                LocalDate.of(2001, 1, 15),
                "Berlin"
        );

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.update(request);

        assertSame(trainee, result);
        assertEquals(LocalDate.of(2001, 1, 15), result.getDateOfBirth());
        assertEquals("Berlin", result.getAddress());

        verify(traineeRepository).findById(1L);
        verify(traineeRepository, never()).update(any());
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateShouldOnlyUpdateDateOfBirthWhenAddressIsNull() {
        Trainee trainee = createTrainee();

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                1L,
                LocalDate.of(2001, 1, 15),
                null
        );

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.update(request);

        assertSame(trainee, result);
        assertEquals(LocalDate.of(2001, 1, 15), result.getDateOfBirth());
        assertEquals("London", result.getAddress());

        verify(traineeRepository).findById(1L);
    }

    @Test
    void updateShouldOnlyUpdateAddressWhenDateOfBirthIsNull() {
        Trainee trainee = createTrainee();

        UpdateTraineeRequest request = new UpdateTraineeRequest(
                1L,
                null,
                "Berlin"
        );

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.update(request);

        assertSame(trainee, result);
        assertEquals(LocalDate.of(2000, 5, 10), result.getDateOfBirth());
        assertEquals("Berlin", result.getAddress());

        verify(traineeRepository).findById(1L);
    }

    @Test
    void updateShouldThrowExceptionWhenTraineeDoesNotExist() {
        UpdateTraineeRequest request = new UpdateTraineeRequest(
                99L,
                LocalDate.of(2001, 1, 15),
                "Berlin"
        );

        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.update(request)
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findById(99L);
    }

    @Test
    void deleteShouldDelegateToRepository() {
        traineeService.delete(1L);

        verify(traineeRepository).deleteById(1L);
    }

    @Test
    void deleteByUsernameShouldDeleteTraineeWhenExists() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername("John.Smith");

        verify(traineeRepository).findByUsername("John.Smith");
        verify(traineeRepository).deleteById(1L);
    }

    @Test
    void deleteByUsernameShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.deleteByUsername("Unknown.User")
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.User");
        verify(traineeRepository, never()).deleteById(anyLong());
    }

    @Test
    void changeActiveStatusShouldChangeTrueToFalse() {
        Trainee trainee = createTrainee();
        trainee.getUser().setIsActive(true);

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.changeActiveStatus(1L);

        assertSame(trainee, result);
        assertFalse(result.getUser().getIsActive());

        verify(traineeRepository).findById(1L);
    }

    @Test
    void changeActiveStatusShouldChangeFalseToTrue() {
        Trainee trainee = createTrainee();
        trainee.getUser().setIsActive(false);

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.changeActiveStatus(1L);

        assertSame(trainee, result);
        assertTrue(result.getUser().getIsActive());

        verify(traineeRepository).findById(1L);
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.changeActiveStatus(99L)
        );

        assertEquals("Trainee not found. id=99", exception.getMessage());

        verify(traineeRepository).findById(99L);
    }

    @Test
    void changePasswordShouldChangePasswordWhenOldPasswordIsCorrect() {
        Trainee trainee = createTrainee();
        trainee.getUser().setPassword("oldPassword");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        traineeService.changePassword(
                "John.Smith",
                "oldPassword",
                "newPassword"
        );

        assertEquals("newPassword", trainee.getUser().getPassword());

        verify(traineeRepository).findByUsername("John.Smith");
    }

    @Test
    void changePasswordShouldThrowExceptionWhenOldPasswordIsIncorrect() {
        Trainee trainee = createTrainee();
        trainee.getUser().setPassword("oldPassword");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.changePassword(
                        "John.Smith",
                        "wrongPassword",
                        "newPassword"
                )
        );

        assertEquals("Old password is incorrect", exception.getMessage());
        assertEquals("oldPassword", trainee.getUser().getPassword());

        verify(traineeRepository).findByUsername("John.Smith");
    }

    @Test
    void changePasswordShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.changePassword(
                        "Unknown.User",
                        "oldPassword",
                        "newPassword"
                )
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.User");
    }

    @Test
    void updateTrainersListShouldReplaceOldTrainersWithNewTrainers() {
        Trainee trainee = createTrainee();

        Trainer oldTrainer = createTrainer(10L, "Old.Trainer");
        Trainer newTrainer1 = createTrainer(20L, "New.Trainer1");
        Trainer newTrainer2 = createTrainer(30L, "New.Trainer2");

        trainee.addTrainer(oldTrainer);

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "John.Smith",
                List.of("New.Trainer1", "New.Trainer2")
        );

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("New.Trainer1"))
                .thenReturn(Optional.of(newTrainer1));

        when(trainerRepository.findByUsername("New.Trainer2"))
                .thenReturn(Optional.of(newTrainer2));

        Trainee result = traineeService.updateTrainersList(request);

        assertSame(trainee, result);

        assertEquals(2, result.getTrainers().size());
        assertFalse(result.getTrainers().contains(oldTrainer));
        assertTrue(result.getTrainers().contains(newTrainer1));
        assertTrue(result.getTrainers().contains(newTrainer2));

        assertFalse(oldTrainer.getTrainees().contains(trainee));
        assertTrue(newTrainer1.getTrainees().contains(trainee));
        assertTrue(newTrainer2.getTrainees().contains(trainee));

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findByUsername("New.Trainer1");
        verify(trainerRepository).findByUsername("New.Trainer2");
    }

    @Test
    void updateTrainersListShouldClearTrainersWhenRequestListIsEmpty() {
        Trainee trainee = createTrainee();

        Trainer oldTrainer1 = createTrainer(10L, "Old.Trainer1");
        Trainer oldTrainer2 = createTrainer(11L, "Old.Trainer2");

        trainee.addTrainer(oldTrainer1);
        trainee.addTrainer(oldTrainer2);

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "John.Smith",
                List.of()
        );

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        Trainee result = traineeService.updateTrainersList(request);

        assertSame(trainee, result);
        assertTrue(result.getTrainers().isEmpty());

        assertFalse(oldTrainer1.getTrainees().contains(trainee));
        assertFalse(oldTrainer2.getTrainees().contains(trainee));

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository, never()).findByUsername(anyString());
    }

    @Test
    void updateTrainersListShouldThrowExceptionWhenTraineeDoesNotExist() {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "Unknown.User",
                List.of("Trainer.One")
        );

        when(traineeRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.updateTrainersList(request)
        );

        assertEquals("Trainee not found. username=Unknown.User", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.User");
        verify(trainerRepository, never()).findByUsername(anyString());
    }

    @Test
    void updateTrainersListShouldThrowExceptionWhenTrainerDoesNotExist() {
        Trainee trainee = createTrainee();

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "John.Smith",
                List.of("Unknown.Trainer")
        );

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("Unknown.Trainer"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.updateTrainersList(request)
        );

        assertEquals("Trainer not found. username=Unknown.Trainer", exception.getMessage());

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findByUsername("Unknown.Trainer");
    }

    private Trainee createTrainee() {
        return Trainee.builder()
                .id(1L)
                .user(createUser(1L, "John.Smith"))
                .dateOfBirth(LocalDate.of(2000, 5, 10))
                .address("London")
                .build();
    }

    private Trainer createTrainer(Long id, String username) {
        return Trainer.builder()
                .id(id)
                .user(createUser(id + 100, username))
                .specialization(createTrainingType())
                .build();
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

    private TrainingType createTrainingType() {
        return TrainingType.builder()
                .id(1L)
                .trainingTypeName("Cardio")
                .build();
    }
}