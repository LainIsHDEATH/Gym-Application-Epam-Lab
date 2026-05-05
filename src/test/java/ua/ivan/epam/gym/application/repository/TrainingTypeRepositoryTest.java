package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.TrainingType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<TrainingType> trainingTypeQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    private TrainingTypeRepository trainingTypeRepository;

    @BeforeEach
    void setUp() throws Exception {
        trainingTypeRepository = new TrainingTypeRepository();

        Field emField = TrainingTypeRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(trainingTypeRepository, em);
    }

    @Test
    void findByIdShouldReturnTrainingTypeWhenExists() {
        TrainingType trainingType = createTrainingType();

        when(em.find(TrainingType.class, 1L)).thenReturn(trainingType);

        Optional<TrainingType> result = trainingTypeRepository.findById(1L);

        assertTrue(result.isPresent());
        assertSame(trainingType, result.get());

        verify(em).find(TrainingType.class, 1L);
    }

    @Test
    void findByIdShouldReturnEmptyWhenTrainingTypeDoesNotExist() {
        when(em.find(TrainingType.class, 99L)).thenReturn(null);

        Optional<TrainingType> result = trainingTypeRepository.findById(99L);

        assertTrue(result.isEmpty());

        verify(em).find(TrainingType.class, 99L);
    }

    @Test
    void findAllShouldReturnAllTrainingTypes() {
        TrainingType cardio = createTrainingType();

        TrainingType strength = TrainingType.builder()
                .id(2L)
                .trainingTypeName("Strength")
                .build();

        when(em.createQuery(anyString(), eq(TrainingType.class))).thenReturn(trainingTypeQuery);
        when(trainingTypeQuery.getResultList()).thenReturn(List.of(cardio, strength));

        List<TrainingType> result = trainingTypeRepository.findAll();

        assertEquals(2, result.size());
        assertSame(cardio, result.get(0));
        assertSame(strength, result.get(1));

        verify(em).createQuery(anyString(), eq(TrainingType.class));
        verify(trainingTypeQuery).getResultList();
    }

    @Test
    void findAllShouldReturnEmptyListWhenNoTrainingTypesExist() {
        when(em.createQuery(anyString(), eq(TrainingType.class))).thenReturn(trainingTypeQuery);
        when(trainingTypeQuery.getResultList()).thenReturn(List.of());

        List<TrainingType> result = trainingTypeRepository.findAll();

        assertTrue(result.isEmpty());

        verify(em).createQuery(anyString(), eq(TrainingType.class));
        verify(trainingTypeQuery).getResultList();
    }

    @Test
    void findByNameShouldReturnTrainingTypeWhenExists() {
        TrainingType trainingType = createTrainingType();

        when(em.createQuery(anyString(), eq(TrainingType.class))).thenReturn(trainingTypeQuery);
        when(trainingTypeQuery.setParameter("name", "Cardio")).thenReturn(trainingTypeQuery);
        when(trainingTypeQuery.getResultList()).thenReturn(List.of(trainingType));

        Optional<TrainingType> result = trainingTypeRepository.findByName("Cardio");

        assertTrue(result.isPresent());
        assertSame(trainingType, result.get());

        verify(em).createQuery(anyString(), eq(TrainingType.class));
        verify(trainingTypeQuery).setParameter("name", "Cardio");
        verify(trainingTypeQuery).getResultList();
    }

    @Test
    void findByNameShouldReturnEmptyWhenTrainingTypeDoesNotExist() {
        when(em.createQuery(anyString(), eq(TrainingType.class))).thenReturn(trainingTypeQuery);
        when(trainingTypeQuery.setParameter("name", "Unknown")).thenReturn(trainingTypeQuery);
        when(trainingTypeQuery.getResultList()).thenReturn(List.of());

        Optional<TrainingType> result = trainingTypeRepository.findByName("Unknown");

        assertTrue(result.isEmpty());

        verify(em).createQuery(anyString(), eq(TrainingType.class));
        verify(trainingTypeQuery).setParameter("name", "Unknown");
        verify(trainingTypeQuery).getResultList();
    }

    @Test
    void existsByIdShouldReturnTrueWhenTrainingTypeExists() {
        mockExistsById(1L, true);

        boolean result = trainingTypeRepository.existsById(1L);

        assertTrue(result);

        verify(countQuery).setParameter("id", 1L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void existsByIdShouldReturnFalseWhenTrainingTypeDoesNotExist() {
        mockExistsById(99L, false);

        boolean result = trainingTypeRepository.existsById(99L);

        assertFalse(result);

        verify(countQuery).setParameter("id", 99L);
        verify(countQuery).getSingleResult();
    }

    private void mockExistsById(Long id, boolean exists) {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", id)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(exists ? 1L : 0L);
    }

    private TrainingType createTrainingType() {
        return TrainingType.builder()
                .id(1L)
                .trainingTypeName("Cardio")
                .build();
    }
}