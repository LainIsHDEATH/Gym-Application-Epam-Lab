package ua.ivan.epam.gym.application.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.ivan.epam.gym.application.model.Trainee;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class TraineeDaoTest {

    private TraineeDao traineeDao;

    @BeforeEach
    void setUp() {
        traineeDao = new TraineeDao();
        traineeDao.setStorage(new ConcurrentHashMap<>());
        traineeDao.setIdGenerator(new AtomicLong(0));
    }

    @Test
    void saveShouldAssignIdAndStoreTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUserId(1L);
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("London");

        Trainee saved = traineeDao.save(trainee);

        assertEquals(1L, saved.getId());
        assertEquals("London", saved.getAddress());
    }

    @Test
    void findByIdShouldReturnTraineeWhenExists() {
        Trainee trainee = new Trainee();
        Trainee saved = traineeDao.save(trainee);

        Optional<Trainee> result = traineeDao.findById(saved.getId());

        assertTrue(result.isPresent());
    }

    @Test
    void updateShouldUpdateExistingTrainee() {
        Trainee trainee = new Trainee();
        trainee.setAddress("London");

        Trainee saved = traineeDao.save(trainee);
        saved.setAddress("Berlin");

        Trainee updated = traineeDao.update(saved);

        assertEquals("Berlin", updated.getAddress());
    }

    @Test
    void updateShouldThrowExceptionWhenTraineeDoesNotExist() {
        Trainee trainee = new Trainee();
        trainee.setId(99L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> traineeDao.update(trainee)
        );

        assertEquals("Trainee not found", exception.getMessage());
    }

    @Test
    void deleteByIdShouldRemoveTrainee() {
        Trainee trainee = new Trainee();
        Trainee saved = traineeDao.save(trainee);

        traineeDao.deleteById(saved.getId());

        assertTrue(traineeDao.findById(saved.getId()).isEmpty());
    }
}