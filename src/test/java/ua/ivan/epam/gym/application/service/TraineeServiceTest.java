package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.request.ChangeActiveStatusRequest;
import ua.ivan.epam.gym.application.dto.request.RegisterTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.dto.response.TraineeProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.mapper.TraineeMapper;
import ua.ivan.epam.gym.application.mapper.TrainerMapper;
import ua.ivan.epam.gym.application.mapper.UserMapper;
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
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TraineeMapper traineeMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void registerShouldCreateUserAndTraineeProfileAndReturnRegistrationResponse() {
        RegisterTraineeProfileRequest request = new RegisterTraineeProfileRequest(
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        RegistrationResponse response = new RegistrationResponse(
                "John.Smith",
                "password12"
        );

        when(usernameGenerator.generate(eq("John"), eq("Smith"), any()))
                .thenReturn("John.Smith");

        when(passwordGenerator.generate())
                .thenReturn("password12");

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

        when(userMapper.toRegistrationResponse(any(User.class)))
                .thenReturn(response);

        RegistrationResponse result = traineeService.register(request);

        assertSame(response, result);
        assertEquals("John.Smith", result.username());
        assertEquals("password12", result.password());

        verify(usernameGenerator).generate(eq("John"), eq("Smith"), any());
        verify(passwordGenerator).generate();

        verify(userRepository).save(argThat(user ->
                user.getFirstName().equals("John")
                        && user.getLastName().equals("Smith")
                        && user.getUsername().equals("John.Smith")
                        && user.getPassword().equals("password12")
                        && user.getIsActive()
        ));

        verify(traineeRepository).save(argThat(trainee ->
                trainee.getUser() != null
                        && trainee.getUser().getUsername().equals("John.Smith")
                        && trainee.getDateOfBirth().equals(LocalDate.of(2000, 5, 10))
                        && trainee.getAddress().equals("London")
        ));

        verify(userMapper).toRegistrationResponse(any(User.class));
    }

    @Test
    void registerShouldPassUsernameExistsPredicateToUsernameGenerator() {
        RegisterTraineeProfileRequest request = new RegisterTraineeProfileRequest(
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        RegistrationResponse response = new RegistrationResponse(
                "John.Smith1",
                "password12"
        );

        when(usernameGenerator.generate(eq("John"), eq("Smith"), any()))
                .thenAnswer(invocation -> {
                    Predicate<String> predicate = invocation.getArgument(2);

                    when(userRepository.existsByUsername("John.Smith"))
                            .thenReturn(true);

                    assertTrue(predicate.test("John.Smith"));

                    return "John.Smith1";
                });

        when(passwordGenerator.generate())
                .thenReturn("password12");

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

        when(userMapper.toRegistrationResponse(any(User.class)))
                .thenReturn(response);

        RegistrationResponse result = traineeService.register(request);

        assertEquals("John.Smith1", result.username());

        verify(userRepository).existsByUsername("John.Smith");
        verify(usernameGenerator).generate(eq("John"), eq("Smith"), any());
    }

    @Test
    void getProfileByUsernameShouldReturnMappedProfileWhenTraineeExists() {
        Trainee trainee = createTrainee();
        TraineeProfileResponse response = createTraineeProfileResponse();

        when(traineeRepository.findProfileByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(traineeMapper.toTraineeProfileResponse(trainee))
                .thenReturn(response);

        TraineeProfileResponse result = traineeService.getProfileByUsername("John.Smith");

        assertSame(response, result);
        assertEquals("John.Smith", result.username());

        verify(traineeRepository).findProfileByUsername("John.Smith");
        verify(traineeMapper).toTraineeProfileResponse(trainee);
    }

    @Test
    void getProfileByUsernameShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findProfileByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.getProfileByUsername("Unknown.User")
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findProfileByUsername("Unknown.User");
        verifyNoInteractions(traineeMapper);
    }

    @Test
    void getShouldReturnTraineeWhenExists() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findById(1L))
                .thenReturn(Optional.of(trainee));

        Trainee result = traineeService.get(1L);

        assertSame(trainee, result);
        assertEquals(1L, result.getId());

        verify(traineeRepository).findById(1L);
    }

    @Test
    void getShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findById(99L))
                .thenReturn(Optional.empty());

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
    void updateShouldUpdateTraineeAndUserFieldsAndReturnMappedProfile() {
        Trainee trainee = createTrainee();
        TraineeProfileResponse response = new TraineeProfileResponse(
                "John.Smith",
                "Johnny",
                "Smithson",
                LocalDate.of(2001, 1, 15),
                "Berlin",
                false,
                List.of()
        );

        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "John.Smith",
                "Johnny",
                "Smithson",
                LocalDate.of(2001, 1, 15),
                "Berlin",
                false
        );

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(traineeMapper.toTraineeProfileResponse(trainee))
                .thenReturn(response);

        TraineeProfileResponse result = traineeService.update(request);

        assertSame(response, result);

        assertEquals(LocalDate.of(2001, 1, 15), trainee.getDateOfBirth());
        assertEquals("Berlin", trainee.getAddress());
        assertEquals("Johnny", trainee.getUser().getFirstName());
        assertEquals("Smithson", trainee.getUser().getLastName());
        assertFalse(trainee.getUser().getIsActive());

        verify(traineeRepository).findByUsername("John.Smith");
        verify(traineeMapper).toTraineeProfileResponse(trainee);
        verify(traineeRepository, never()).save(any());
        verify(traineeRepository, never()).update(any());
    }

    @Test
    void updateShouldNotOverwriteNullableFieldsWhenTheyAreNull() {
        Trainee trainee = createTrainee();
        TraineeProfileResponse response = createTraineeProfileResponse();

        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "John.Smith",
                null,
                null,
                null,
                null,
                null
        );

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(traineeMapper.toTraineeProfileResponse(trainee))
                .thenReturn(response);

        TraineeProfileResponse result = traineeService.update(request);

        assertSame(response, result);

        assertEquals(LocalDate.of(2000, 5, 10), trainee.getDateOfBirth());
        assertEquals("London", trainee.getAddress());
        assertEquals("John", trainee.getUser().getFirstName());
        assertEquals("Smith", trainee.getUser().getLastName());
        assertTrue(trainee.getUser().getIsActive());

        verify(traineeRepository).findByUsername("John.Smith");
        verify(traineeMapper).toTraineeProfileResponse(trainee);
    }

    @Test
    void updateShouldThrowExceptionWhenTraineeDoesNotExist() {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "Unknown.User",
                "John",
                "Smith",
                LocalDate.of(2001, 1, 15),
                "Berlin",
                true
        );

        when(traineeRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.update(request)
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.User");
        verifyNoInteractions(traineeMapper);
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
    void changeActiveStatusShouldSetActiveToFalse() {
        Trainee trainee = createTrainee();
        trainee.getUser().setIsActive(true);

        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "John.Smith",
                false
        );

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        traineeService.changeActiveStatus(request);

        assertFalse(trainee.getUser().getIsActive());

        verify(traineeRepository).findByUsername("John.Smith");
    }

    @Test
    void changeActiveStatusShouldSetActiveToTrue() {
        Trainee trainee = createTrainee();
        trainee.getUser().setIsActive(false);

        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "John.Smith",
                true
        );

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        traineeService.changeActiveStatus(request);

        assertTrue(trainee.getUser().getIsActive());

        verify(traineeRepository).findByUsername("John.Smith");
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenTraineeDoesNotExist() {
        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "Unknown.User",
                true
        );

        when(traineeRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeService.changeActiveStatus(request)
        );

        assertEquals("Trainee not found. username=Unknown.User", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.User");
    }

    @Test
    void updateTrainersListShouldReplaceOldTrainersWithNewTrainersAndReturnMappedResponse() {
        Trainee trainee = createTrainee();

        Trainer oldTrainer = createTrainer(10L, "Old.Trainer");
        Trainer newTrainer1 = createTrainer(20L, "New.Trainer1");
        Trainer newTrainer2 = createTrainer(30L, "New.Trainer2");

        trainee.addTrainer(oldTrainer);

        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "John.Smith",
                List.of("New.Trainer1", "New.Trainer2")
        );

        TrainerShortResponse response1 = createTrainerShortResponse("New.Trainer1");
        TrainerShortResponse response2 = createTrainerShortResponse("New.Trainer2");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("New.Trainer1"))
                .thenReturn(Optional.of(newTrainer1));

        when(trainerRepository.findByUsername("New.Trainer2"))
                .thenReturn(Optional.of(newTrainer2));

        when(trainerMapper.toTrainerShortResponse(newTrainer1))
                .thenReturn(response1);

        when(trainerMapper.toTrainerShortResponse(newTrainer2))
                .thenReturn(response2);

        List<TrainerShortResponse> result = traineeService.updateTrainersList(request);

        assertEquals(2, result.size());
        assertTrue(result.contains(response1));
        assertTrue(result.contains(response2));

        assertEquals(2, trainee.getTrainers().size());
        assertFalse(trainee.getTrainers().contains(oldTrainer));
        assertTrue(trainee.getTrainers().contains(newTrainer1));
        assertTrue(trainee.getTrainers().contains(newTrainer2));

        assertFalse(oldTrainer.getTrainees().contains(trainee));
        assertTrue(newTrainer1.getTrainees().contains(trainee));
        assertTrue(newTrainer2.getTrainees().contains(trainee));

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findByUsername("New.Trainer1");
        verify(trainerRepository).findByUsername("New.Trainer2");
        verify(trainerMapper).toTrainerShortResponse(newTrainer1);
        verify(trainerMapper).toTrainerShortResponse(newTrainer2);
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

        List<TrainerShortResponse> result = traineeService.updateTrainersList(request);

        assertTrue(result.isEmpty());
        assertTrue(trainee.getTrainers().isEmpty());

        assertFalse(oldTrainer1.getTrainees().contains(trainee));
        assertFalse(oldTrainer2.getTrainees().contains(trainee));

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository, never()).findByUsername(anyString());
        verifyNoInteractions(trainerMapper);
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
        verifyNoInteractions(trainerMapper);
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
        verifyNoInteractions(trainerMapper);
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
        String[] parts = username.split("\\.");

        return User.builder()
                .id(id)
                .firstName(parts[0])
                .lastName(parts[1])
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

    private TraineeProfileResponse createTraineeProfileResponse() {
        return new TraineeProfileResponse(
                "John.Smith",
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London",
                true,
                List.of()
        );
    }

    private TrainerShortResponse createTrainerShortResponse(String username) {
        String[] parts = username.split("\\.");

        return new TrainerShortResponse(
                username,
                parts[0],
                parts[1],
                new TrainingTypeResponse(1L, "Cardio")
        );
    }
}