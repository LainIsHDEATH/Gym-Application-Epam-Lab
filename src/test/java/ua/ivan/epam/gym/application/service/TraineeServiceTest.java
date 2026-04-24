package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dao.CrudDao;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.utils.PasswordGenerator;
import ua.ivan.epam.gym.application.utils.UsernameGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private CrudDao<Long, Trainee> traineeDao;

    @Mock
    private CrudDao<Long, User> userDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeService(
                traineeDao,
                userDao,
                usernameGenerator,
                passwordGenerator
        );
    }

    @Test
    void createShouldCreateUserAndTraineeProfile() {
        LocalDate birthDate = LocalDate.of(2000, 5, 10);

        when(userDao.findAll()).thenReturn(List.of());
        when(usernameGenerator.generate("John", "Smith", List.of()))
                .thenReturn("John.Smith");
        when(passwordGenerator.generate())
                .thenReturn("password12");

        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        when(traineeDao.save(any(Trainee.class))).thenAnswer(invocation -> {
            Trainee trainee = invocation.getArgument(0);
            trainee.setId(1L);
            return trainee;
        });

        Trainee result = traineeService.create(
                "John",
                "Smith",
                birthDate,
                "London"
        );

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(birthDate, result.getDateOfBirth());
        assertEquals("London", result.getAddress());

        verify(userDao).save(any(User.class));
        verify(traineeDao).save(any(Trainee.class));
    }

    @Test
    void getShouldReturnTraineeWhenExists() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);

        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.get(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getShouldThrowExceptionWhenTraineeDoesNotExist() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> traineeService.get(99L)
        );

        assertEquals("Trainee not found", exception.getMessage());
    }

    @Test
    void updateShouldDelegateToDao() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setAddress("Berlin");

        when(traineeDao.update(trainee)).thenReturn(trainee);

        Trainee result = traineeService.update(trainee);

        assertEquals("Berlin", result.getAddress());
        verify(traineeDao).update(trainee);
    }

    @Test
    void deleteShouldDelegateToDao() {
        traineeService.delete(1L);

        verify(traineeDao).deleteById(1L);
    }
}