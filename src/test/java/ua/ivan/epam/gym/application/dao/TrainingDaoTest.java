package ua.ivan.epam.gym.application.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.ivan.epam.gym.application.model.Training;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TrainingDaoTest {

    private TrainingDao trainingDao;

    @BeforeEach
    void setUp() {
        trainingDao = new TrainingDao();
        trainingDao.setStorage(new ConcurrentHashMap<>());
        trainingDao.setIdGenerator(new AtomicLong(0));
    }

    @Test
    void saveShouldAssignIdAndStoreTraining() {
        Training training = new Training();
        training.setTrainingName("Morning Cardio");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);

        Training saved = trainingDao.save(training);

        assertEquals(1L, saved.getId());
        assertEquals("Morning Cardio", saved.getTrainingName());
    }

    @Test
    void findByIdShouldReturnTrainingWhenExists() {
        Training saved = trainingDao.save(new Training());

        assertTrue(trainingDao.findById(saved.getId()).isPresent());
    }

    @Test
    void updateShouldUpdateExistingTraining() {
        Training saved = trainingDao.save(new Training());
        saved.setTrainingName("Updated Training");

        Training updated = trainingDao.update(saved);

        assertEquals("Updated Training", updated.getTrainingName());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainingDoesNotExist() {
        Training training = new Training();
        training.setId(99L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingDao.update(training)
        );

        assertEquals("Training not found", exception.getMessage());
    }

    @Test
    void deleteByIdShouldRemoveTraining() {
        Training saved = trainingDao.save(new Training());

        trainingDao.deleteById(saved.getId());

        assertTrue(trainingDao.findById(saved.getId()).isEmpty());
    }
}