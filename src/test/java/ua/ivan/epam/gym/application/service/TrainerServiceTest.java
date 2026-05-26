package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.request.ChangeActiveStatusRequest;
import ua.ivan.epam.gym.application.dto.request.RegisterTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.dto.response.TraineeShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.mapper.TrainerMapper;
import ua.ivan.epam.gym.application.mapper.UserMapper;
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
    private UserRepository userRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UserMapper userMapper;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void registerShouldCreateUserAndTrainerProfileAndReturnRegistrationResponse() {
        RegisterTrainerProfileRequest request = new RegisterTrainerProfileRequest(
                "Mike",
                "Brown",
                1L
        );

        TrainingType specialization = createTrainingType(1L, "Fitness");

        RegistrationResponse response = new RegistrationResponse(
                "Mike.Brown",
                "password12"
        );

        when(trainingTypeRepository.findById(1L))
                .thenReturn(Optional.of(specialization));

        when(usernameGenerator.generate(eq("Mike"), eq("Brown"), any()))
                .thenReturn("Mike.Brown");

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

        when(userMapper.toRegistrationResponse(any(User.class)))
                .thenReturn(response);

        RegistrationResponse result = trainerService.register(request);

        assertSame(response, result);
        assertEquals("Mike.Brown", result.username());
        assertEquals("password12", result.password());

        verify(trainingTypeRepository).findById(1L);

        verify(usernameGenerator).generate(eq("Mike"), eq("Brown"), any());
        verify(passwordGenerator).generate();

        verify(userRepository).save(argThat(user ->
                user.getFirstName().equals("Mike")
                        && user.getLastName().equals("Brown")
                        && user.getUsername().equals("Mike.Brown")
                        && user.getPassword().equals("password12")
                        && user.getIsActive()
        ));

        verify(trainerRepository).save(argThat(trainer ->
                trainer.getUser() != null
                        && trainer.getUser().getUsername().equals("Mike.Brown")
                        && trainer.getSpecialization() == specialization
        ));

        verify(userMapper).toRegistrationResponse(any(User.class));
    }

    @Test
    void registerShouldPassUsernameExistsPredicateToUsernameGenerator() {
        RegisterTrainerProfileRequest request = new RegisterTrainerProfileRequest(
                "Mike",
                "Brown",
                1L
        );

        TrainingType specialization = createTrainingType(1L, "Fitness");

        RegistrationResponse response = new RegistrationResponse(
                "Mike.Brown1",
                "password12"
        );

        when(trainingTypeRepository.findById(1L))
                .thenReturn(Optional.of(specialization));

        when(usernameGenerator.generate(eq("Mike"), eq("Brown"), any()))
                .thenAnswer(invocation -> {
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

        when(userMapper.toRegistrationResponse(any(User.class)))
                .thenReturn(response);

        RegistrationResponse result = trainerService.register(request);

        assertEquals("Mike.Brown1", result.username());

        verify(userRepository).existsByUsername("Mike.Brown");
        verify(usernameGenerator).generate(eq("Mike"), eq("Brown"), any());
    }

    @Test
    void registerShouldThrowExceptionWhenTrainingTypeDoesNotExist() {
        RegisterTrainerProfileRequest request = new RegisterTrainerProfileRequest(
                "Mike",
                "Brown",
                99L
        );

        when(trainingTypeRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.register(request)
        );

        assertEquals("Training type not found. id=99", exception.getMessage());

        verify(trainingTypeRepository).findById(99L);
        verify(userRepository, never()).save(any());
        verify(trainerRepository, never()).save(any());
        verify(usernameGenerator, never()).generate(anyString(), anyString(), any());
        verify(passwordGenerator, never()).generate();
        verifyNoInteractions(userMapper);
    }

    @Test
    void getProfileByUsernameShouldReturnMappedProfileWhenTrainerExists() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");

        TrainerProfileResponse response = createTrainerProfileResponse();

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainerMapper.toTrainerProfileResponse(trainer))
                .thenReturn(response);

        TrainerProfileResponse result = trainerService.getProfileByUsername("Mike.Brown");

        assertSame(response, result);
        assertEquals("Mike.Brown", result.username());

        verify(trainerRepository).findByUsername("Mike.Brown");
        verify(trainerMapper).toTrainerProfileResponse(trainer);
    }

    @Test
    void getProfileByUsernameShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.getProfileByUsername("Unknown.User")
        );

        assertEquals("Trainer not found", exception.getMessage());

        verify(trainerRepository).findByUsername("Unknown.User");
        verifyNoInteractions(trainerMapper);
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
    void updateShouldUpdateTrainerUserFieldsAndReturnMappedProfile() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");

        TrainerProfileResponse response = new TrainerProfileResponse(
                "Mike.Brown",
                "Michael",
                "Black",
                new TrainingTypeResponse(1L, "Fitness"),
                false,
                List.of()
        );

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(
                "Mike.Brown",
                "Michael",
                "Black",
                false
        );

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainerMapper.toTrainerProfileResponse(trainer))
                .thenReturn(response);

        TrainerProfileResponse result = trainerService.update(request);

        assertSame(response, result);

        assertEquals("Michael", trainer.getUser().getFirstName());
        assertEquals("Black", trainer.getUser().getLastName());
        assertFalse(trainer.getUser().getIsActive());

        assertEquals(1L, trainer.getSpecialization().getId());
        assertEquals("Fitness", trainer.getSpecialization().getTrainingTypeName());

        verify(trainerRepository).findByUsername("Mike.Brown");
        verify(trainerMapper).toTrainerProfileResponse(trainer);
        verify(trainingTypeRepository, never()).findById(anyLong());
        verify(trainerRepository, never()).save(any());
        verify(trainerRepository, never()).update(any());
    }

    @Test
    void updateShouldNotOverwriteNullableFieldsWhenTheyAreNull() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");

        TrainerProfileResponse response = createTrainerProfileResponse();

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(
                "Mike.Brown",
                null,
                null,
                null
        );

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainerMapper.toTrainerProfileResponse(trainer))
                .thenReturn(response);

        TrainerProfileResponse result = trainerService.update(request);

        assertSame(response, result);

        assertEquals("Mike", trainer.getUser().getFirstName());
        assertEquals("Brown", trainer.getUser().getLastName());
        assertTrue(trainer.getUser().getIsActive());

        verify(trainerRepository).findByUsername("Mike.Brown");
        verify(trainerMapper).toTrainerProfileResponse(trainer);
    }

    @Test
    void updateShouldThrowExceptionWhenTrainerDoesNotExist() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(
                "Unknown.User",
                "Mike",
                "Brown",
                true
        );

        when(trainerRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.update(request)
        );

        assertEquals("Trainer not found", exception.getMessage());

        verify(trainerRepository).findByUsername("Unknown.User");
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void changeActiveStatusShouldSetActiveToFalse() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        trainer.getUser().setIsActive(true);

        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "Mike.Brown",
                false
        );

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        trainerService.changeActiveStatus(request);

        assertFalse(trainer.getUser().getIsActive());

        verify(trainerRepository).findByUsername("Mike.Brown");
    }

    @Test
    void changeActiveStatusShouldSetActiveToTrue() {
        Trainer trainer = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        trainer.getUser().setIsActive(false);

        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "Mike.Brown",
                true
        );

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        trainerService.changeActiveStatus(request);

        assertTrue(trainer.getUser().getIsActive());

        verify(trainerRepository).findByUsername("Mike.Brown");
    }

    @Test
    void changeActiveStatusShouldThrowExceptionWhenTrainerDoesNotExist() {
        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "Unknown.User",
                true
        );

        when(trainerRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.changeActiveStatus(request)
        );

        assertEquals("Trainer not found. username=Unknown.User", exception.getMessage());

        verify(trainerRepository).findByUsername("Unknown.User");
    }

    @Test
    void getTrainersNotAssignedToTraineeShouldReturnMappedTrainersWhenTraineeExists() {
        Trainee trainee = createTrainee(1L, "John.Smith");

        Trainer trainer1 = createTrainer(1L, "Mike.Brown", 1L, "Fitness");
        Trainer trainer2 = createTrainer(2L, "Alice.White", 2L, "Yoga");

        TrainerShortResponse response1 = createTrainerShortResponse("Mike.Brown", 1L, "Fitness");
        TrainerShortResponse response2 = createTrainerShortResponse("Alice.White", 2L, "Yoga");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findNotAssignedToTrainee("John.Smith"))
                .thenReturn(List.of(trainer1, trainer2));

        when(trainerMapper.toTrainerShortResponse(trainer1))
                .thenReturn(response1);

        when(trainerMapper.toTrainerShortResponse(trainer2))
                .thenReturn(response2);

        List<TrainerShortResponse> result =
                trainerService.getTrainersNotAssignedToTrainee("John.Smith");

        assertEquals(2, result.size());
        assertSame(response1, result.get(0));
        assertSame(response2, result.get(1));

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findNotAssignedToTrainee("John.Smith");
        verify(trainerMapper).toTrainerShortResponse(trainer1);
        verify(trainerMapper).toTrainerShortResponse(trainer2);
    }

    @Test
    void getTrainersNotAssignedToTraineeShouldReturnEmptyListWhenNoTrainersFound() {
        Trainee trainee = createTrainee(1L, "John.Smith");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findNotAssignedToTrainee("John.Smith"))
                .thenReturn(List.of());

        List<TrainerShortResponse> result =
                trainerService.getTrainersNotAssignedToTrainee("John.Smith");

        assertTrue(result.isEmpty());

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findNotAssignedToTrainee("John.Smith");
        verifyNoInteractions(trainerMapper);
    }

    @Test
    void getTrainersNotAssignedToTraineeShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUsername("Unknown.Trainee"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerService.getTrainersNotAssignedToTrainee("Unknown.Trainee")
        );

        assertEquals("Trainee not found. username=Unknown.Trainee", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.Trainee");
        verify(trainerRepository, never()).findNotAssignedToTrainee(anyString());
        verifyNoInteractions(trainerMapper);
    }

    private Trainer createTrainer(Long id,
                                  String username,
                                  Long specializationId,
                                  String specializationName) {
        Trainer trainer = new Trainer();
        trainer.setId(id);
        trainer.setUser(createUser(id + 100, username));
        trainer.setSpecialization(createTrainingType(specializationId, specializationName));

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

    private TrainingType createTrainingType(Long id, String name) {
        return TrainingType.builder()
                .id(id)
                .trainingTypeName(name)
                .build();
    }

    private TrainerProfileResponse createTrainerProfileResponse() {
        return new TrainerProfileResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                new TrainingTypeResponse(1L, "Fitness"),
                true,
                List.of()
        );
    }

    private TrainerShortResponse createTrainerShortResponse(String username,
                                                            Long specializationId,
                                                            String specializationName) {
        String[] parts = username.split("\\.");

        return new TrainerShortResponse(
                username,
                parts[0],
                parts[1],
                new TrainingTypeResponse(specializationId, specializationName)
        );
    }

    private TraineeShortResponse createTraineeShortResponse(String username) {
        String[] parts = username.split("\\.");

        return new TraineeShortResponse(
                username,
                parts[0],
                parts[1]
        );
    }
}