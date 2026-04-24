package ua.ivan.epam.gym.application.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.ivan.epam.gym.application.model.Trainer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TrainerDaoTest {

    private TrainerDao trainerDao;

    @BeforeEach
    void setUp() {
        trainerDao = new TrainerDao();
        trainerDao.setStorage(new ConcurrentHashMap<>());
        trainerDao.setIdGenerator(new AtomicLong(0));
    }

    @Test
    void saveShouldAssignIdAndStoreTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUserId(1L);
        trainer.setSpecialization("Fitness");

        Trainer saved = trainerDao.save(trainer);

        assertEquals(1L, saved.getId());
        assertEquals("Fitness", saved.getSpecialization());
    }

    @Test
    void findByIdShouldReturnTrainerWhenExists() {
        Trainer saved = trainerDao.save(new Trainer());

        assertTrue(trainerDao.findById(saved.getId()).isPresent());
    }

    @Test
    void updateShouldUpdateExistingTrainer() {
        Trainer saved = trainerDao.save(new Trainer());
        saved.setSpecialization("Yoga");

        Trainer updated = trainerDao.update(saved);

        assertEquals("Yoga", updated.getSpecialization());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainerDoesNotExist() {
        Trainer trainer = new Trainer();
        trainer.setId(99L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainerDao.update(trainer)
        );

        assertEquals("Trainer not found", exception.getMessage());
    }

    @Test
    void deleteByIdShouldRemoveTrainer() {
        Trainer saved = trainerDao.save(new Trainer());

        trainerDao.deleteById(saved.getId());

        assertTrue(trainerDao.findById(saved.getId()).isEmpty());
    }
}