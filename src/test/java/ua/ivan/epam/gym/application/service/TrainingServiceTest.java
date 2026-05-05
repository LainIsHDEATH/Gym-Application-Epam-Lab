package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.CreateTrainingRequest;
import ua.ivan.epam.gym.application.dto.GetTraineeTrainingsRequest;
import ua.ivan.epam.gym.application.dto.GetTrainerTrainingsRequest;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.TraineeRepository;
import ua.ivan.epam.gym.application.repository.TrainerRepository;
import ua.ivan.epam.gym.application.repository.TrainingRepository;
import ua.ivan.epam.gym.application.repository.TrainingTypeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void createShouldCreateTrainingWhenReferencesExist() {
        LocalDate date = LocalDate.of(2026, 4, 24);

        CreateTrainingRequest request = new CreateTrainingRequest(
                1L,
                2L,
                "Morning Cardio",
                3L,
                date,
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown");
        TrainingType trainingType = createTrainingType(3L, "Cardio");

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(trainingType));

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(10L);
            return training;
        });

        Training result = trainingService.create(request);

        assertEquals(10L, result.getId());
        assertSame(trainee, result.getTrainee());
        assertSame(trainer, result.getTrainer());
        assertSame(trainingType, result.getTrainingType());
        assertEquals("Morning Cardio", result.getTrainingName());
        assertEquals(date, result.getTrainingDate());
        assertEquals(60, result.getTrainingDuration());

        assertTrue(trainee.getTrainers().contains(trainer));
        assertTrue(trainer.getTrainees().contains(trainee));

        verify(traineeRepository).findById(1L);
        verify(trainerRepository).findById(2L);
        verify(trainingTypeRepository).findById(3L);
        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void createShouldPassCorrectTrainingToRepositorySave() {
        LocalDate date = LocalDate.of(2026, 4, 24);

        CreateTrainingRequest request = new CreateTrainingRequest(
                1L,
                2L,
                "Morning Cardio",
                3L,
                date,
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown");
        TrainingType trainingType = createTrainingType(3L, "Cardio");

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(trainingType));

        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);

            assertSame(trainee, training.getTrainee());
            assertSame(trainer, training.getTrainer());
            assertSame(trainingType, training.getTrainingType());
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
        CreateTrainingRequest request = new CreateTrainingRequest(
                1L,
                2L,
                "Morning Cardio",
                3L,
                LocalDate.of(2026, 4, 24),
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown");
        TrainingType trainingType = createTrainingType(3L, "Cardio");

        trainee.addTrainer(trainer);

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(3L)).thenReturn(Optional.of(trainingType));

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
        CreateTrainingRequest request = new CreateTrainingRequest(
                99L,
                2L,
                "Training",
                3L,
                LocalDate.now(),
                60
        );

        when(traineeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingService.create(request)
        );

        assertEquals("Trainee not found", exception.getMessage());

        verify(traineeRepository).findById(99L);
        verify(trainerRepository, never()).findById(anyLong());
        verify(trainingTypeRepository, never()).findById(anyLong());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createShouldThrowExceptionWhenTrainerDoesNotExist() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                1L,
                99L,
                "Training",
                3L,
                LocalDate.now(),
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingService.create(request)
        );

        assertEquals("Trainer not found", exception.getMessage());

        verify(traineeRepository).findById(1L);
        verify(trainerRepository).findById(99L);
        verify(trainingTypeRepository, never()).findById(anyLong());
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void createShouldThrowExceptionWhenTrainingTypeDoesNotExist() {
        CreateTrainingRequest request = new CreateTrainingRequest(
                1L,
                2L,
                "Training",
                99L,
                LocalDate.now(),
                60
        );

        Trainee trainee = createTrainee(1L, "John.Smith");
        Trainer trainer = createTrainer(2L, "Mike.Brown");

        when(traineeRepository.findById(1L)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findById(2L)).thenReturn(Optional.of(trainer));
        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingService.create(request)
        );

        assertEquals("Training type not found", exception.getMessage());

        verify(traineeRepository).findById(1L);
        verify(trainerRepository).findById(2L);
        verify(trainingTypeRepository).findById(99L);
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void getShouldReturnTrainingWhenExists() {
        Training training = createTraining();

        when(trainingRepository.findById(1L)).thenReturn(Optional.of(training));

        Training result = trainingService.get(1L);

        assertSame(training, result);
        assertEquals(1L, result.getId());

        verify(trainingRepository).findById(1L);
    }

    @Test
    void getShouldThrowExceptionWhenTrainingDoesNotExist() {
        when(trainingRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
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
                .trainer(createTrainer(2L, "Trainer.Two"))
                .trainingType(createTrainingType(2L, "Strength"))
                .trainingName("Strength Training")
                .trainingDate(LocalDate.of(2026, 5, 6))
                .trainingDuration(45)
                .build();

        when(trainingRepository.findAll()).thenReturn(List.of(training1, training2));

        List<Training> result = trainingService.getAll();

        assertEquals(2, result.size());
        assertSame(training1, result.get(0));
        assertSame(training2, result.get(1));

        verify(trainingRepository).findAll();
    }

    @Test
    void getAllShouldReturnEmptyListWhenNoTrainingsExist() {
        when(trainingRepository.findAll()).thenReturn(List.of());

        List<Training> result = trainingService.getAll();

        assertTrue(result.isEmpty());

        verify(trainingRepository).findAll();
    }

    @Test
    void getTraineeTrainingsShouldDelegateToRepositoryWithCriteria() {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                "John.Smith",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Mike",
                1L
        );

        Training training = createTraining();

        when(trainingRepository.findTraineeTrainingsByCriteria(
                "John.Smith",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Mike",
                1L
        )).thenReturn(List.of(training));

        List<Training> result = trainingService.getTraineeTrainings(request);

        assertEquals(1, result.size());
        assertSame(training, result.getFirst());

        verify(trainingRepository).findTraineeTrainingsByCriteria(
                "John.Smith",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Mike",
                1L
        );
    }

    @Test
    void getTraineeTrainingsShouldSupportNullOptionalCriteria() {
        GetTraineeTrainingsRequest request = new GetTraineeTrainingsRequest(
                "John.Smith",
                null,
                null,
                null,
                null
        );

        when(trainingRepository.findTraineeTrainingsByCriteria(
                "John.Smith",
                null,
                null,
                null,
                null
        )).thenReturn(List.of());

        List<Training> result = trainingService.getTraineeTrainings(request);

        assertTrue(result.isEmpty());

        verify(trainingRepository).findTraineeTrainingsByCriteria(
                "John.Smith",
                null,
                null,
                null,
                null
        );
    }

    @Test
    void getTrainerTrainingsShouldDelegateToRepositoryWithCriteria() {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                "Mike.Brown",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "John"
        );

        Training training = createTraining();

        when(trainingRepository.findTrainerTrainingsByCriteria(
                "Mike.Brown",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "John"
        )).thenReturn(List.of(training));

        List<Training> result = trainingService.getTrainerTrainings(request);

        assertEquals(1, result.size());
        assertSame(training, result.getFirst());

        verify(trainingRepository).findTrainerTrainingsByCriteria(
                "Mike.Brown",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "John"
        );
    }

    @Test
    void getTrainerTrainingsShouldSupportNullOptionalCriteria() {
        GetTrainerTrainingsRequest request = new GetTrainerTrainingsRequest(
                "Mike.Brown",
                null,
                null,
                null
        );

        when(trainingRepository.findTrainerTrainingsByCriteria(
                "Mike.Brown",
                null,
                null,
                null
        )).thenReturn(List.of());

        List<Training> result = trainingService.getTrainerTrainings(request);

        assertTrue(result.isEmpty());

        verify(trainingRepository).findTrainerTrainingsByCriteria(
                "Mike.Brown",
                null,
                null,
                null
        );
    }

    private Training createTraining() {
        return Training.builder()
                .id(1L)
                .trainee(createTrainee(1L, "John.Smith"))
                .trainer(createTrainer(2L, "Mike.Brown"))
                .trainingType(createTrainingType(3L, "Cardio"))
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

        trainee.getUser().setTrainee(trainee);

        return trainee;
    }

    private Trainer createTrainer(Long id, String username) {
        Trainer trainer = Trainer.builder()
                .id(id)
                .user(createUser(id + 200, username))
                .specialization(createTrainingType(1L, "Cardio"))
                .build();

        trainer.getUser().setTrainer(trainer);

        return trainer;
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