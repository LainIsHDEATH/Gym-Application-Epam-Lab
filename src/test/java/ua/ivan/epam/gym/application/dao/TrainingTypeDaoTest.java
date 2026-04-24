package ua.ivan.epam.gym.application.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.ivan.epam.gym.application.model.TrainingType;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TrainingTypeDaoTest {

    private TrainingTypeDao trainingTypeDao;

    @BeforeEach
    void setUp() {
        trainingTypeDao = new TrainingTypeDao();
        trainingTypeDao.setStorage(new ConcurrentHashMap<>());
        trainingTypeDao.setIdGenerator(new AtomicLong(0));
    }

    @Test
    void saveShouldAssignIdAndStoreTrainingType() {
        TrainingType trainingType = new TrainingType();
        trainingType.setTrainingTypeName("Cardio");

        TrainingType saved = trainingTypeDao.save(trainingType);

        assertEquals(1L, saved.getId());
        assertEquals("Cardio", saved.getTrainingTypeName());
    }

    @Test
    void findByIdShouldReturnTrainingTypeWhenExists() {
        TrainingType saved = trainingTypeDao.save(new TrainingType());

        assertTrue(trainingTypeDao.findById(saved.getId()).isPresent());
    }

    @Test
    void findAllShouldReturnAllTrainingTypes() {
        TrainingType cardio = new TrainingType();
        cardio.setTrainingTypeName("Cardio");

        TrainingType strength = new TrainingType();
        strength.setTrainingTypeName("Strength");

        trainingTypeDao.save(cardio);
        trainingTypeDao.save(strength);

        assertEquals(2, trainingTypeDao.findAll().size());
    }

    @Test
    void updateShouldUpdateExistingTrainingType() {
        TrainingType saved = trainingTypeDao.save(new TrainingType());
        saved.setTrainingTypeName("Updated Type");

        TrainingType updated = trainingTypeDao.update(saved);

        assertEquals("Updated Type", updated.getTrainingTypeName());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainingTypeDoesNotExist() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(99L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainingTypeDao.update(trainingType)
        );

        assertEquals("Training type not found", exception.getMessage());
    }

    @Test
    void deleteByIdShouldRemoveTrainingType() {
        TrainingType saved = trainingTypeDao.save(new TrainingType());

        trainingTypeDao.deleteById(saved.getId());

        assertTrue(trainingTypeDao.findById(saved.getId()).isEmpty());
    }
}