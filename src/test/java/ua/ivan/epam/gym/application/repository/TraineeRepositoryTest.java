package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.User;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Trainee> traineeQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    private TraineeRepository traineeRepository;

    @BeforeEach
    void setUp() throws Exception {
        traineeRepository = new TraineeRepository();

        Field emField = TraineeRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(traineeRepository, em);
    }

    @Test
    void saveShouldPersistAndReturnTrainee() {
        Trainee trainee = createTrainee();

        Trainee saved = traineeRepository.save(trainee);

        assertSame(trainee, saved);
        verify(em).persist(trainee);
    }

    @Test
    void findByIdShouldReturnTraineeWhenExists() {
        Trainee trainee = createTrainee();

        when(em.find(Trainee.class, 1L)).thenReturn(trainee);

        Optional<Trainee> result = traineeRepository.findById(1L);

        assertTrue(result.isPresent());
        assertSame(trainee, result.get());
        verify(em).find(Trainee.class, 1L);
    }

    @Test
    void findByIdShouldReturnEmptyWhenTraineeDoesNotExist() {
        when(em.find(Trainee.class, 99L)).thenReturn(null);

        Optional<Trainee> result = traineeRepository.findById(99L);

        assertTrue(result.isEmpty());
        verify(em).find(Trainee.class, 99L);
    }

    @Test
    void findByUsernameShouldReturnTraineeWhenExists() {
        Trainee trainee = createTrainee();

        when(em.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "John.Smith")).thenReturn(traineeQuery);
        when(traineeQuery.getResultList()).thenReturn(List.of(trainee));

        Optional<Trainee> result = traineeRepository.findByUsername("John.Smith");

        assertTrue(result.isPresent());
        assertSame(trainee, result.get());

        verify(em).createQuery(anyString(), eq(Trainee.class));
        verify(traineeQuery).setParameter("username", "John.Smith");
        verify(traineeQuery).getResultList();
    }

    @Test
    void findByUsernameShouldReturnEmptyWhenTraineeDoesNotExist() {
        when(em.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.setParameter("username", "Unknown.User")).thenReturn(traineeQuery);
        when(traineeQuery.getResultList()).thenReturn(List.of());

        Optional<Trainee> result = traineeRepository.findByUsername("Unknown.User");

        assertTrue(result.isEmpty());

        verify(em).createQuery(anyString(), eq(Trainee.class));
        verify(traineeQuery).setParameter("username", "Unknown.User");
        verify(traineeQuery).getResultList();
    }

    @Test
    void findAllShouldReturnAllTrainees() {
        Trainee trainee1 = createTrainee();

        Trainee trainee2 = Trainee.builder()
                .id(2L)
                .dateOfBirth(LocalDate.of(1999, 1, 1))
                .address("Kyiv")
                .user(createUser(2L, "Mike.Brown"))
                .build();

        when(em.createQuery(anyString(), eq(Trainee.class))).thenReturn(traineeQuery);
        when(traineeQuery.getResultList()).thenReturn(List.of(trainee1, trainee2));

        List<Trainee> result = traineeRepository.findAll();

        assertEquals(2, result.size());
        assertSame(trainee1, result.get(0));
        assertSame(trainee2, result.get(1));

        verify(em).createQuery(anyString(), eq(Trainee.class));
        verify(traineeQuery).getResultList();
    }

    @Test
    void updateShouldMergeAndReturnUpdatedTraineeWhenTraineeExists() {
        Trainee trainee = createTrainee();

        Trainee mergedTrainee = Trainee.builder()
                .id(1L)
                .dateOfBirth(LocalDate.of(2001, 1, 15))
                .address("Berlin")
                .user(createUser(1L, "John.Smith"))
                .build();

        mockExistsById(1L, true);

        when(em.merge(trainee)).thenReturn(mergedTrainee);

        Trainee result = traineeRepository.update(trainee);

        assertSame(mergedTrainee, result);
        assertEquals("Berlin", result.getAddress());
        assertEquals(LocalDate.of(2001, 1, 15), result.getDateOfBirth());

        verify(em).merge(trainee);
    }

    @Test
    void updateShouldThrowExceptionWhenTraineeIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> traineeRepository.update(null)
        );

        assertEquals("Trainee and trainee id must not be null", exception.getMessage());
        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTraineeIdIsNull() {
        Trainee trainee = createTrainee();
        trainee.setId(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> traineeRepository.update(trainee)
        );

        assertEquals("Trainee and trainee id must not be null", exception.getMessage());
        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTraineeDoesNotExist() {
        Trainee trainee = createTrainee();
        trainee.setId(99L);

        mockExistsById(99L, false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> traineeRepository.update(trainee)
        );

        assertEquals("Trainee not found. id=99", exception.getMessage());
        verify(em, never()).merge(any());
    }

    @Test
    void deleteByIdShouldRemoveTraineeWhenTraineeExists() {
        Trainee trainee = createTrainee();

        when(em.find(Trainee.class, 1L)).thenReturn(trainee);

        traineeRepository.deleteById(1L);

        verify(em).find(Trainee.class, 1L);
        verify(em).remove(trainee);
    }

    @Test
    void deleteByIdShouldNotThrowExceptionWhenTraineeDoesNotExist() {
        when(em.find(Trainee.class, 99L)).thenReturn(null);

        doThrow(new IllegalArgumentException())
                .when(em)
                .remove(isNull());

        assertDoesNotThrow(() -> traineeRepository.deleteById(99L));

        verify(em).find(Trainee.class, 99L);
        verify(em).remove(null);
    }

    @Test
    void existsByIdShouldReturnTrueWhenTraineeExists() {
        mockExistsById(1L, true);

        boolean result = traineeRepository.existsById(1L);

        assertTrue(result);
        verify(countQuery).setParameter("id", 1L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void existsByIdShouldReturnFalseWhenTraineeDoesNotExist() {
        mockExistsById(99L, false);

        boolean result = traineeRepository.existsById(99L);

        assertFalse(result);
        verify(countQuery).setParameter("id", 99L);
        verify(countQuery).getSingleResult();
    }

    private void mockExistsById(Long id, boolean exists) {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", id)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(exists ? 1L : 0L);
    }

    private Trainee createTrainee() {
        return Trainee.builder()
                .id(1L)
                .dateOfBirth(LocalDate.of(2000, 5, 10))
                .address("London")
                .user(createUser(1L, "John.Smith"))
                .build();
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
}