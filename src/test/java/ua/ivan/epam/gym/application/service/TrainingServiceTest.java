package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.request.AddTrainingRequest;
import ua.ivan.epam.gym.application.dto.response.TraineeTrainingResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerTrainingResponse;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.mapper.TrainingMapper;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.TraineeRepository;
import ua.ivan.epam.gym.application.repository.TrainerRepository;
import ua.ivan.epam.gym.application.repository.TrainingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingMapper trainingMapper;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createShouldCreateTrainingWhenTraineeAndTrainerExist() {
        LocalDate date = LocalDate.of(2026, 5, 5);

        AddTrainingRequest request = new AddTrainingRequest(
                "John.Smith",
                "Mike.Brown",
                "Morning Cardio",
                date,
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown", 3L, "Cardio");
        TrainingType expectedTrainingType = trainer.getSpecialization();

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(10L);
            return training;
        });

        Training result = trainingService.create(request);

        assertEquals(10L, result.getId());
        assertSame(trainee, result.getTrainee());
        assertSame(trainer, result.getTrainer());
        assertSame(expectedTrainingType, result.getTrainingType());
        assertEquals("Morning Cardio", result.getTrainingName());
        assertEquals(date, result.getTrainingDate());
        assertEquals(60, result.getTrainingDuration());

        assertTrue(trainee.getTrainers().contains(trainer));
        assertTrue(trainer.getTrainees().contains(trainee));

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findByUsername("Mike.Brown");
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createShouldUseTrainerSpecializationAsTrainingType() {
        AddTrainingRequest request = new AddTrainingRequest(
                "John.Smith",
                "Mike.Brown",
                "Morning Cardio",
                LocalDate.of(2026, 5, 5),
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown", 3L, "Cardio");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);

            assertSame(trainer.getSpecialization(), training.getTrainingType());
            assertEquals(3L, training.getTrainingType().getId());
            assertEquals("Cardio", training.getTrainingType().getTrainingTypeName());

            training.setId(10L);
            return training;
        });

        Training result = trainingService.create(request);

        assertEquals(10L, result.getId());

        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createShouldPassCorrectTrainingToRepositorySave() {
        LocalDate date = LocalDate.of(2026, 5, 5);

        AddTrainingRequest request = new AddTrainingRequest(
                "John.Smith",
                "Mike.Brown",
                "Morning Cardio",
                date,
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown", 3L, "Cardio");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);

            assertSame(trainee, training.getTrainee());
            assertSame(trainer, training.getTrainer());
            assertSame(trainer.getSpecialization(), training.getTrainingType());
            assertEquals("Morning Cardio", training.getTrainingName());
            assertEquals(date, training.getTrainingDate());
            assertEquals(60, training.getTrainingDuration());

            training.setId(10L);
            return training;
        });

        Training result = trainingService.create(request);

        assertEquals(10L, result.getId());

        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createShouldNotDuplicateTrainerAssignmentWhenAlreadyAssigned() {
        AddTrainingRequest request = new AddTrainingRequest(
                "John.Smith",
                "Mike.Brown",
                "Morning Cardio",
                LocalDate.of(2026, 5, 5),
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown", 3L, "Cardio");

        trainee.addTrainer(trainer);

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(trainer));

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(10L);
            return training;
        });

        Training result = trainingService.create(request);

        assertEquals(10L, result.getId());
        assertEquals(1, trainee.getTrainers().size());
        assertEquals(1, trainer.getTrainees().size());
        assertTrue(trainee.getTrainers().contains(trainer));
        assertTrue(trainer.getTrainees().contains(trainee));

        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createShouldThrowExceptionWhenTraineeDoesNotExist() {
        AddTrainingRequest request = new AddTrainingRequest(
                "Unknown.Trainee",
                "Mike.Brown",
                "Training",
                LocalDate.now(),
                60
        );

        when(traineeRepository.findByUsername("Unknown.Trainee"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.create(request)
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findByUsername("Unknown.Trainee");
        verify(trainerRepository, never()).findByUsername(anyString());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createShouldThrowExceptionWhenTrainerDoesNotExist() {
        AddTrainingRequest request = new AddTrainingRequest(
                "John.Smith",
                "Unknown.Trainer",
                "Training",
                LocalDate.now(),
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");

        when(traineeRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("Unknown.Trainer"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.create(request)
        );

        assertEquals("Trainer not found", exception.getMessage());

        verify(traineeRepository).findByUsername("John.Smith");
        verify(trainerRepository).findByUsername("Unknown.Trainer");
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getShouldReturnTrainingWhenExists() {
        Training training = createTraining();

        when(trainingRepository.findById(1L))
                .thenReturn(Optional.of(training));

        Training result = trainingService.get(1L);

        assertSame(training, result);
        assertEquals(1L, result.getId());

        verify(trainingRepository).findById(1L);
    }

    @Test
    void getShouldThrowExceptionWhenTrainingDoesNotExist() {
        when(trainingRepository.findById(99L))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainingService.get(99L)
        );

        assertEquals("Training not found", exception.getMessage());

        verify(trainingRepository).findById(99L);
    }

    @Test
    void getAllShouldReturnTrainings() {
        Training training1 = createTraining();

        Training training2 = Training.builder()
                .id(2L)
                .trainee(createTrainee(2L, "Trainee.Two"))
                .trainer(createTrainer(3L, "Trainer.Two", 2L, "Strength"))
                .trainingType(createTrainingType(2L, "Strength"))
                .trainingName("Strength Training")
                .trainingDate(LocalDate.of(2026, 5, 6))
                .trainingDuration(45)
                .build();

        when(trainingRepository.findAll())
                .thenReturn(List.of(training1, training2));

        List<Training> result = trainingService.getAll();

        assertEquals(2, result.size());
        assertSame(training1, result.get(0));
        assertSame(training2, result.get(1));

        verify(trainingRepository).findAll();
    }

    @Test
    void getAllShouldReturnEmptyListWhenNoTrainingsExist() {
        when(trainingRepository.findAll())
                .thenReturn(List.of());

        List<Training> result = trainingService.getAll();

        assertTrue(result.isEmpty());

        verify(trainingRepository).findAll();
    }

    @Test
    void getTraineeTrainingsShouldDelegateToRepositoryAndMapResponses() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);

        Training training = createTraining();

        TraineeTrainingResponse response = new TraineeTrainingResponse(
                "Morning Cardio",
                LocalDate.of(2026, 5, 5),
                new TrainingTypeResponse(3L, "Cardio"),
                60,
                "Mike.Brown"
        );

        when(trainingRepository.findTraineeTrainingsByCriteria(
                "John.Smith",
                from,
                to,
                "Mike",
                3L
        )).thenReturn(List.of(training));

        when(trainingMapper.toTraineeTrainingResponse(training))
                .thenReturn(response);

        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(
                "John.Smith",
                from,
                to,
                "Mike",
                3L
        );

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(trainingRepository).findTraineeTrainingsByCriteria(
                "John.Smith",
                from,
                to,
                "Mike",
                3L
        );
        verify(trainingMapper).toTraineeTrainingResponse(training);
    }

    @Test
    void getTraineeTrainingsShouldSupportNullOptionalCriteria() {
        when(trainingRepository.findTraineeTrainingsByCriteria(
                "John.Smith",
                null,
                null,
                null,
                null
        )).thenReturn(List.of());

        List<TraineeTrainingResponse> result = trainingService.getTraineeTrainings(
                "John.Smith",
                null,
                null,
                null,
                null
        );

        assertTrue(result.isEmpty());

        verify(trainingRepository).findTraineeTrainingsByCriteria(
                "John.Smith",
                null,
                null,
                null,
                null
        );
        verifyNoInteractions(trainingMapper);
    }

    @Test
    void getTrainerTrainingsShouldDelegateToRepositoryAndMapResponses() {
        LocalDate from = LocalDate.of(2026, 5, 1);
        LocalDate to = LocalDate.of(2026, 5, 31);

        Training training = createTraining();

        TrainerTrainingResponse response = new TrainerTrainingResponse(
                "Morning Cardio",
                LocalDate.of(2026, 5, 5),
                new TrainingTypeResponse(3L, "Cardio"),
                60,
                "John.Smith"
        );

        when(trainingRepository.findTrainerTrainingsByCriteria(
                "Mike.Brown",
                from,
                to,
                "John"
        )).thenReturn(List.of(training));

        when(trainingMapper.toTrainerTrainingResponse(training))
                .thenReturn(response);

        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(
                "Mike.Brown",
                from,
                to,
                "John"
        );

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(trainingRepository).findTrainerTrainingsByCriteria(
                "Mike.Brown",
                from,
                to,
                "John"
        );
        verify(trainingMapper).toTrainerTrainingResponse(training);
    }

    @Test
    void getTrainerTrainingsShouldSupportNullOptionalCriteria() {
        when(trainingRepository.findTrainerTrainingsByCriteria(
                "Mike.Brown",
                null,
                null,
                null
        )).thenReturn(List.of());

        List<TrainerTrainingResponse> result = trainingService.getTrainerTrainings(
                "Mike.Brown",
                null,
                null,
                null
        );

        assertTrue(result.isEmpty());

        verify(trainingRepository).findTrainerTrainingsByCriteria(
                "Mike.Brown",
                null,
                null,
                null
        );
        verifyNoInteractions(trainingMapper);
    }

    private Training createTraining() {
        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown", 3L, "Cardio");

        return Training.builder()
                .id(1L)
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainer.getSpecialization())
                .trainingName("Morning Cardio")
                .trainingDate(LocalDate.of(2026, 5, 5))
                .trainingDuration(60)
                .build();
    }

    private Trainee createTrainee(Long id, String username) {
        Trainee trainee = Trainee.builder()
                .id(id)
                .user(createUser(id + 100, username))
                .dateOfBirth(LocalDate.of(2000, 5, 10))
                .address("London")
                .build();

        return trainee;
    }

    private Trainer createTrainer(Long id,
                                  String username,
                                  Long specializationId,
                                  String specializationName) {
        Trainer trainer = Trainer.builder()
                .id(id)
                .user(createUser(id + 200, username))
                .specialization(createTrainingType(specializationId, specializationName))
                .build();

        return trainer;
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
}