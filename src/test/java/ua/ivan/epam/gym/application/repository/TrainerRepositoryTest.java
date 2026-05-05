package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Trainer> trainerQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    private TrainerRepository trainerRepository;

    @BeforeEach
    void setUp() throws Exception {
        trainerRepository = new TrainerRepository();

        Field emField = TrainerRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(trainerRepository, em);
    }

    @Test
    void saveShouldPersistAndReturnTrainer() {
        Trainer trainer = createTrainer();

        Trainer saved = trainerRepository.save(trainer);

        assertSame(trainer, saved);
        verify(em).persist(trainer);
    }

    @Test
    void findByIdShouldReturnTrainerWhenExists() {
        Trainer trainer = createTrainer();

        when(em.find(Trainer.class, 1L)).thenReturn(trainer);

        Optional<Trainer> result = trainerRepository.findById(1L);

        assertTrue(result.isPresent());
        assertSame(trainer, result.get());

        verify(em).find(Trainer.class, 1L);
    }

    @Test
    void findByIdShouldReturnEmptyWhenTrainerDoesNotExist() {
        when(em.find(Trainer.class, 99L)).thenReturn(null);

        Optional<Trainer> result = trainerRepository.findById(99L);

        assertTrue(result.isEmpty());

        verify(em).find(Trainer.class, 99L);
    }

    @Test
    void findByUsernameShouldReturnTrainerWhenExists() {
        Trainer trainer = createTrainer();

        when(em.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "John.Smith")).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(List.of(trainer));

        Optional<Trainer> result = trainerRepository.findByUsername("John.Smith");

        assertTrue(result.isPresent());
        assertSame(trainer, result.get());

        verify(em).createQuery(anyString(), eq(Trainer.class));
        verify(trainerQuery).setParameter("username", "John.Smith");
        verify(trainerQuery).getResultList();
    }

    @Test
    void findByUsernameShouldReturnEmptyWhenTrainerDoesNotExist() {
        when(em.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("username", "Unknown.User")).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(List.of());

        Optional<Trainer> result = trainerRepository.findByUsername("Unknown.User");

        assertTrue(result.isEmpty());

        verify(em).createQuery(anyString(), eq(Trainer.class));
        verify(trainerQuery).setParameter("username", "Unknown.User");
        verify(trainerQuery).getResultList();
    }

    @Test
    void findAllShouldReturnAllTrainers() {
        Trainer trainer1 = createTrainer();

        Trainer trainer2 = Trainer.builder()
                .id(2L)
                .user(createUser(2L, "Mike.Brown"))
                .specialization(createTrainingType(2L, "Strength"))
                .build();

        when(em.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(List.of(trainer1, trainer2));

        List<Trainer> result = trainerRepository.findAll();

        assertEquals(2, result.size());
        assertSame(trainer1, result.get(0));
        assertSame(trainer2, result.get(1));

        verify(em).createQuery(anyString(), eq(Trainer.class));
        verify(trainerQuery).getResultList();
    }

    @Test
    void updateShouldMergeAndReturnUpdatedTrainerWhenTrainerExists() {
        Trainer trainer = createTrainer();

        Trainer mergedTrainer = Trainer.builder()
                .id(1L)
                .user(createUser(1L, "John.Smith"))
                .specialization(createTrainingType(2L, "Strength"))
                .build();

        mockExistsById(1L, true);

        when(em.merge(trainer)).thenReturn(mergedTrainer);

        Trainer result = trainerRepository.update(trainer);

        assertSame(mergedTrainer, result);
        assertEquals(2L, result.getSpecialization().getId());
        assertEquals("Strength", result.getSpecialization().getTrainingTypeName());

        verify(em).merge(trainer);
    }

    @Test
    void updateShouldThrowExceptionWhenTrainerIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainerRepository.update(null)
        );

        assertEquals("Trainer and trainer id must not be null", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainerIdIsNull() {
        Trainer trainer = createTrainer();
        trainer.setId(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainerRepository.update(trainer)
        );

        assertEquals("Trainer and trainer id must not be null", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainerDoesNotExist() {
        Trainer trainer = createTrainer();
        trainer.setId(99L);

        mockExistsById(99L, false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerRepository.update(trainer)
        );

        assertEquals("Trainer not found. id=99", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void deleteByIdShouldRemoveTrainerWhenTrainerExists() {
        Trainer trainer = createTrainer();

        when(em.find(Trainer.class, 1L)).thenReturn(trainer);

        trainerRepository.deleteById(1L);

        verify(em).find(Trainer.class, 1L);
        verify(em).remove(trainer);
    }

    @Test
    void deleteByIdShouldNotThrowExceptionWhenTrainerDoesNotExist() {
        when(em.find(Trainer.class, 99L)).thenReturn(null);

        doThrow(new IllegalArgumentException())
                .when(em)
                .remove(isNull());

        assertDoesNotThrow(() -> trainerRepository.deleteById(99L));

        verify(em).find(Trainer.class, 99L);
        verify(em).remove(null);
    }

    @Test
    void existsByIdShouldReturnTrueWhenTrainerExists() {
        mockExistsById(1L, true);

        boolean result = trainerRepository.existsById(1L);

        assertTrue(result);

        verify(countQuery).setParameter("id", 1L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void existsByIdShouldReturnFalseWhenTrainerDoesNotExist() {
        mockExistsById(99L, false);

        boolean result = trainerRepository.existsById(99L);

        assertFalse(result);

        verify(countQuery).setParameter("id", 99L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void findNotAssignedToTraineeShouldReturnTrainers() {
        Trainer trainer1 = createTrainer();

        Trainer trainer2 = Trainer.builder()
                .id(2L)
                .user(createUser(2L, "Mike.Brown"))
                .specialization(createTrainingType(2L, "Strength"))
                .build();

        when(em.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("traineeUsername", "Trainee.User")).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(List.of(trainer1, trainer2));

        List<Trainer> result = trainerRepository.findNotAssignedToTrainee("Trainee.User");

        assertEquals(2, result.size());
        assertSame(trainer1, result.get(0));
        assertSame(trainer2, result.get(1));

        verify(em).createQuery(anyString(), eq(Trainer.class));
        verify(trainerQuery).setParameter("traineeUsername", "Trainee.User");
        verify(trainerQuery).getResultList();
    }

    @Test
    void findNotAssignedToTraineeShouldReturnEmptyListWhenNoTrainersFound() {
        when(em.createQuery(anyString(), eq(Trainer.class))).thenReturn(trainerQuery);
        when(trainerQuery.setParameter("traineeUsername", "Trainee.User")).thenReturn(trainerQuery);
        when(trainerQuery.getResultList()).thenReturn(List.of());

        List<Trainer> result = trainerRepository.findNotAssignedToTrainee("Trainee.User");

        assertTrue(result.isEmpty());

        verify(em).createQuery(anyString(), eq(Trainer.class));
        verify(trainerQuery).setParameter("traineeUsername", "Trainee.User");
        verify(trainerQuery).getResultList();
    }

    private void mockExistsById(Long id, boolean exists) {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", id)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(exists ? 1L : 0L);
    }

    private Trainer createTrainer() {
        return Trainer.builder()
                .id(1L)
                .user(createUser(1L, "John.Smith"))
                .specialization(createTrainingType(1L, "Cardio"))
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

    private TrainingType createTrainingType(Long id, String name) {
        return TrainingType.builder()
                .id(id)
                .trainingTypeName(name)
                .build();
    }
}