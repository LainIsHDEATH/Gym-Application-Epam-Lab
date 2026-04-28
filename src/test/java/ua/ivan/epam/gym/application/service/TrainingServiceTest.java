package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dao.CrudDao;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private CrudDao<Long, Training> trainingDao;

    @Mock
    private CrudDao<Long, Trainee> traineeDao;

    @Mock
    private CrudDao<Long, Trainer> trainerDao;

    @Mock
    private CrudDao<Long, TrainingType> trainingTypeDao;

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService(
                trainingDao,
                traineeDao,
                trainerDao,
                trainingTypeDao
        );
    }

    @Test
    void createShouldCreateTrainingWhenReferencesExist() {
        LocalDate date = LocalDate.of(2026, 4, 24);

        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(new Trainer()));
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(new TrainingType()));

        when(trainingDao.save(any(Training.class))).thenAnswer(invocation -> {
            Training training = invocation.getArgument(0);
            training.setId(1L);
            return training;
        });

        Training result = trainingService.create(
                1L,
                1L,
                "Morning Cardio",
                1L,
                date,
                60
        );

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getTraineeId());
        assertEquals(1L, result.getTrainerId());
        assertEquals("Morning Cardio", result.getTrainingName());
        assertEquals(1L, result.getTrainingTypeId());
        assertEquals(date, result.getTrainingDate());
        assertEquals(60, result.getTrainingDuration());

        verify(trainingDao).save(any(Training.class));
    }

    @Test
    void createShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingService.create(
                        99L,
                        1L,
                        "Training",
                        1L,
                        LocalDate.now(),
                        60
                )
        );

        assertEquals("Trainee not found", exception.getMessage());
        verify(trainingDao, never()).save(any());
    }

    @Test
    void createShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingService.create(
                        1L,
                        99L,
                        "Training",
                        1L,
                        LocalDate.now(),
                        60
                )
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainingDao, never()).save(any());
    }

    @Test
    void createShouldThrowExceptionWhenTrainingTypeDoesNotExist() {
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(1L)).thenReturn(Optional.of(new Trainer()));
        when(trainingTypeDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingService.create(
                        1L,
                        1L,
                        "Training",
                        99L,
                        LocalDate.now(),
                        60
                )
        );

        assertEquals("Training type not found", exception.getMessage());
        verify(trainingDao, never()).save(any());
    }

    @Test
    void getShouldReturnTrainingWhenExists() {
        Training training = new Training();
        training.setId(1L);

        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        Training result = trainingService.get(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getShouldThrowExceptionWhenTrainingDoesNotExist() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingService.get(99L)
        );

        assertEquals("Training not found", exception.getMessage());
    }

    @Test
    void getAllShouldReturnTrainings() {
        Training training1 = new Training();
        Training training2 = new Training();

        when(trainingDao.findAll()).thenReturn(List.of(training1, training2));

        List<Training> result = trainingService.getAll();

        assertEquals(2, result.size());
    }
}