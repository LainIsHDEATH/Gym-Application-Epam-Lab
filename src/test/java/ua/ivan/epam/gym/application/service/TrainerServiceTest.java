package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dao.CrudDao;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.utils.PasswordGenerator;
import ua.ivan.epam.gym.application.utils.UsernameGenerator;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private CrudDao<Long, Trainer> trainerDao;

    @Mock
    private CrudDao<Long, User> userDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerService(
                trainerDao,
                userDao,
                usernameGenerator,
                passwordGenerator
        );
    }

    @Test
    void createShouldCreateUserAndTrainerProfile() {
        when(userDao.findAll()).thenReturn(List.of());

        when(usernameGenerator.generate(eq("Mike"), eq("Brown"), anyList()))
                .thenReturn("Mike.Brown");

        when(passwordGenerator.generate())
                .thenReturn("password12");

        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(trainerDao.save(any(Trainer.class))).thenAnswer(invocation -> {
            Trainer trainer = invocation.getArgument(0);
            trainer.setId(1L);
            return trainer;
        });

        Trainer result = trainerService.create(
                "Mike",
                "Brown",
                "Fitness"
        );

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals("Fitness", result.getSpecialization());

        verify(userDao).findAll();
        verify(usernameGenerator).generate(eq("Mike"), eq("Brown"), anyList());
        verify(passwordGenerator).generate();
        verify(userDao).save(any(User.class));
        verify(trainerDao).save(any(Trainer.class));
    }

    @Test
    void getShouldReturnTrainerWhenExists() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);

        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.get(1L);

        assertEquals(1L, result.getId());
        verify(trainerDao).findById(1L);
    }

    @Test
    void getShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trainerService.get(99L)
        );

        assertEquals("Trainer not found", exception.getMessage());
        verify(trainerDao).findById(99L);
    }

    @Test
    void updateShouldDelegateToDao() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setSpecialization("Yoga");

        when(trainerDao.update(trainer)).thenReturn(trainer);

        Trainer result = trainerService.update(trainer);

        assertEquals("Yoga", result.getSpecialization());
        verify(trainerDao).update(trainer);
    }
}