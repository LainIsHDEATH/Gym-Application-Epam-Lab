package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.model.User;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Training> trainingQuery;

    @Mock
    private TypedQuery<Long> countQuery;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private CriteriaQuery<Training> cq;

    @Mock
    private Root<Training> trainingRoot;

    @Mock
    private Join<Training, Trainee> traineeJoin;

    @Mock
    private Join<Trainee, User> traineeUserJoin;

    @Mock
    private Join<Training, Trainer> trainerJoin;

    @Mock
    private Join<Trainer, User> trainerUserJoin;

    @Mock
    private Join<Training, TrainingType> trainingTypeJoin;

    @Mock
    private Path<String> usernamePath;

    @Mock
    private Path<String> firstNamePath;

    @Mock
    private Path<String> lastNamePath;

    @Mock
    private Path<LocalDate> trainingDatePath;

    @Mock
    private Path<Long> trainingTypeIdPath;

    @Mock
    private Predicate predicate;

    @Mock
    private Expression<String> firstNameWithSpaceExpression;

    @Mock
    private Expression<String> fullNameExpression;

    @Mock
    private Expression<String> lowerFullNameExpression;

    @Mock
    private Order order;

    private TrainingRepository trainingRepository;

    @BeforeEach
    void setUp() throws Exception {
        trainingRepository = new TrainingRepository();

        Field emField = TrainingRepository.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(trainingRepository, em);
    }

    @Test
    void saveShouldPersistAndReturnTraining() {
        Training training = createTraining();

        Training saved = trainingRepository.save(training);

        assertSame(training, saved);
        verify(em).persist(training);
    }

    @Test
    void findByIdShouldReturnTrainingWhenExists() {
        Training training = createTraining();

        when(em.find(Training.class, 1L)).thenReturn(training);

        Optional<Training> result = trainingRepository.findById(1L);

        assertTrue(result.isPresent());
        assertSame(training, result.get());

        verify(em).find(Training.class, 1L);
    }

    @Test
    void findByIdShouldReturnEmptyWhenTrainingDoesNotExist() {
        when(em.find(Training.class, 99L)).thenReturn(null);

        Optional<Training> result = trainingRepository.findById(99L);

        assertTrue(result.isEmpty());

        verify(em).find(Training.class, 99L);
    }

    @Test
    void findAllShouldReturnAllTrainings() {
        Training training1 = createTraining();

        Training training2 = Training.builder()
                .id(2L)
                .trainee(createTrainee(2L, "Mike.Brown"))
                .trainer(createTrainer(2L, "Trainer.Two"))
                .trainingType(createTrainingType(2L, "Strength"))
                .trainingName("Strength Training")
                .trainingDate(LocalDate.of(2026, 5, 6))
                .trainingDuration(45)
                .build();

        when(em.createQuery(anyString(), eq(Training.class))).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(List.of(training1, training2));

        List<Training> result = trainingRepository.findAll();

        assertEquals(2, result.size());
        assertSame(training1, result.get(0));
        assertSame(training2, result.get(1));

        verify(em).createQuery(anyString(), eq(Training.class));
        verify(trainingQuery).getResultList();
    }

    @Test
    void updateShouldMergeAndReturnUpdatedTrainingWhenTrainingExists() {
        Training training = createTraining();

        Training updatedTraining = Training.builder()
                .id(1L)
                .trainee(createTrainee(1L, "John.Smith"))
                .trainer(createTrainer(1L, "Trainer.One"))
                .trainingType(createTrainingType(1L, "Cardio"))
                .trainingName("Updated Training")
                .trainingDate(LocalDate.of(2026, 5, 10))
                .trainingDuration(90)
                .build();

        mockExistsById(1L, true);

        when(em.merge(training)).thenReturn(updatedTraining);

        Training result = trainingRepository.update(training);

        assertSame(updatedTraining, result);
        assertEquals("Updated Training", result.getTrainingName());
        assertEquals(90, result.getTrainingDuration());

        verify(em).merge(training);
    }

    @Test
    void updateShouldThrowExceptionWhenTrainingIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainingRepository.update(null)
        );

        assertEquals("Training and training id must not be null", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainingIdIsNull() {
        Training training = createTraining();
        training.setId(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> trainingRepository.update(training)
        );

        assertEquals("Training and training id must not be null", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void updateShouldThrowExceptionWhenTrainingDoesNotExist() {
        Training training = createTraining();
        training.setId(99L);

        mockExistsById(99L, false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainingRepository.update(training)
        );

        assertEquals("Training not found. id=99", exception.getMessage());

        verify(em, never()).merge(any());
    }

    @Test
    void deleteByIdShouldRemoveTrainingWhenTrainingExists() {
        Training training = createTraining();

        when(em.find(Training.class, 1L)).thenReturn(training);

        trainingRepository.deleteById(1L);

        verify(em).find(Training.class, 1L);
        verify(em).remove(training);
    }

    @Test
    void deleteByIdShouldNotThrowExceptionWhenTrainingDoesNotExist() {
        when(em.find(Training.class, 99L)).thenReturn(null);

        doThrow(new IllegalArgumentException())
                .when(em)
                .remove(isNull());

        assertDoesNotThrow(() -> trainingRepository.deleteById(99L));

        verify(em).find(Training.class, 99L);
        verify(em).remove(null);
    }

    @Test
    void existsByIdShouldReturnTrueWhenTrainingExists() {
        mockExistsById(1L, true);

        boolean result = trainingRepository.existsById(1L);

        assertTrue(result);

        verify(countQuery).setParameter("id", 1L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void existsByIdShouldReturnFalseWhenTrainingDoesNotExist() {
        mockExistsById(99L, false);

        boolean result = trainingRepository.existsById(99L);

        assertFalse(result);

        verify(countQuery).setParameter("id", 99L);
        verify(countQuery).getSingleResult();
    }

    @Test
    void findTraineeTrainingsByCriteriaShouldReturnTrainingsWithAllFilters() {
        Training training = createTraining();

        mockCriteriaBase();

        mockTraineeJoinsOnly();
        mockTraineeUsernameFilter("John.Smith");

        mockTrainerJoinsOnly();
        mockTrainingTypeJoinOnly();
        mockTrainingTypeIdFilter(1L);

        mockCriteriaNameLikeForTrainer();
        mockCriteriaDateFilters();
        mockCriteriaFinalQuery(List.of(training));

        List<Training> result = trainingRepository.findTraineeTrainingsByCriteria(
                "John.Smith",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Trainer",
                1L
        );

        assertEquals(1, result.size());
        assertSame(training, result.get(0));

        verify(traineeUserJoin).get("username");
        verify(trainerUserJoin).get("firstName");
        verify(trainerUserJoin).get("lastName");
        verify(trainingTypeJoin).get("id");

        verify(cb).equal(usernamePath, "John.Smith");
        verify(cb).greaterThanOrEqualTo(trainingDatePath, LocalDate.of(2026, 5, 1));
        verify(cb).lessThanOrEqualTo(trainingDatePath, LocalDate.of(2026, 5, 31));
        verify(cb).like(lowerFullNameExpression, "%trainer%");
        verify(cb).equal(trainingTypeIdPath, 1L);

        verify(trainingQuery).getResultList();
    }

    @Test
    void findTraineeTrainingsByCriteriaShouldSkipOptionalFiltersWhenTheyAreNullOrBlank() {
        Training training = createTraining();

        mockCriteriaBase();

        mockTraineeJoinsOnly();
        mockTraineeUsernameFilter("John.Smith");

        mockTrainerJoinsOnly();

        mockTrainingTypeJoinOnly();

        mockCriteriaFinalQuery(List.of(training));

        List<Training> result = trainingRepository.findTraineeTrainingsByCriteria(
                "John.Smith",
                null,
                null,
                "   ",
                null
        );

        assertEquals(1, result.size());
        assertSame(training, result.get(0));

        verify(cb).equal(usernamePath, "John.Smith");

        verify(cb, never()).greaterThanOrEqualTo(any(), any(LocalDate.class));
        verify(cb, never()).lessThanOrEqualTo(any(), any(LocalDate.class));
        verify(cb, never()).like(any(), anyString());
        verify(trainingTypeJoin, never()).get("id");

        verify(trainingQuery).getResultList();
    }

    @Test
    void findTrainerTrainingsByCriteriaShouldReturnTrainingsWithAllFilters() {
        Training training = createTraining();

        mockCriteriaBase();

        mockTrainerJoinsOnly();
        mockTrainerUsernameFilter("Trainer.One");

        mockTraineeJoinsOnly();

        mockCriteriaNameLikeForTrainee();
        mockCriteriaDateFilters();
        mockCriteriaFinalQuery(List.of(training));

        List<Training> result = trainingRepository.findTrainerTrainingsByCriteria(
                "Trainer.One",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "John"
        );

        assertEquals(1, result.size());
        assertSame(training, result.get(0));

        verify(trainerUserJoin).get("username");
        verify(traineeUserJoin).get("firstName");
        verify(traineeUserJoin).get("lastName");

        verify(cb).equal(usernamePath, "Trainer.One");
        verify(cb).greaterThanOrEqualTo(trainingDatePath, LocalDate.of(2026, 5, 1));
        verify(cb).lessThanOrEqualTo(trainingDatePath, LocalDate.of(2026, 5, 31));
        verify(cb).like(lowerFullNameExpression, "%john%");

        verify(trainingQuery).getResultList();
    }

    @Test
    void findTrainerTrainingsByCriteriaShouldSkipOptionalFiltersWhenTheyAreNullOrBlank() {
        Training training = createTraining();

        mockCriteriaBase();

        mockTrainerJoinsOnly();
        mockTrainerUsernameFilter("Trainer.One");

        mockTraineeJoinsOnly();

        mockCriteriaFinalQuery(List.of(training));

        List<Training> result = trainingRepository.findTrainerTrainingsByCriteria(
                "Trainer.One",
                null,
                null,
                "   "
        );

        assertEquals(1, result.size());
        assertSame(training, result.get(0));

        verify(cb).equal(usernamePath, "Trainer.One");

        verify(cb, never()).greaterThanOrEqualTo(any(), any(LocalDate.class));
        verify(cb, never()).lessThanOrEqualTo(any(), any(LocalDate.class));
        verify(cb, never()).like(any(), anyString());

        verify(trainingQuery).getResultList();
    }

    private void mockExistsById(Long id, boolean exists) {
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", id)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(exists ? 1L : 0L);
    }

    private void mockCriteriaBase() {
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Training.class)).thenReturn(cq);
        when(cq.from(Training.class)).thenReturn(trainingRoot);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTraineeCriteriaJoins() {
        doReturn(traineeJoin)
                .when(trainingRoot)
                .join("trainee", JoinType.INNER);

        doReturn(traineeUserJoin)
                .when(traineeJoin)
                .join("user", JoinType.INNER);

        doReturn(usernamePath)
                .when(traineeUserJoin)
                .get("username");

        when(cb.equal(usernamePath, "John.Smith"))
                .thenReturn(predicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTrainerCriteriaJoins() {
        doReturn(trainerJoin)
                .when(trainingRoot)
                .join("trainer", JoinType.INNER);

        doReturn(trainerUserJoin)
                .when(trainerJoin)
                .join("user", JoinType.INNER);

        doReturn(usernamePath)
                .when(trainerUserJoin)
                .get("username");

        when(cb.equal(usernamePath, "Trainer.One"))
                .thenReturn(predicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTraineeJoinsOnly() {
        doReturn(traineeJoin)
                .when(trainingRoot)
                .join("trainee", JoinType.INNER);

        doReturn(traineeUserJoin)
                .when(traineeJoin)
                .join("user", JoinType.INNER);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTrainerJoinsOnly() {
        doReturn(trainerJoin)
                .when(trainingRoot)
                .join("trainer", JoinType.INNER);

        doReturn(trainerUserJoin)
                .when(trainerJoin)
                .join("user", JoinType.INNER);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTraineeUsernameFilter(String username) {
        doReturn(usernamePath)
                .when(traineeUserJoin)
                .get("username");

        when(cb.equal(usernamePath, username))
                .thenReturn(predicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTrainerUsernameFilter(String username) {
        doReturn(usernamePath)
                .when(trainerUserJoin)
                .get("username");

        when(cb.equal(usernamePath, username))
                .thenReturn(predicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTrainingTypeJoinOnly() {
        doReturn(trainingTypeJoin)
                .when(trainingRoot)
                .join("trainingType", JoinType.INNER);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockTrainingTypeIdFilter(Long trainingTypeId) {
        doReturn(trainingTypeIdPath)
                .when(trainingTypeJoin)
                .get("id");

        when(cb.equal(trainingTypeIdPath, trainingTypeId))
                .thenReturn(predicate);
    }

    private void mockCriteriaDateFilters() {
        when(cb.greaterThanOrEqualTo(trainingDatePath, LocalDate.of(2026, 5, 1)))
                .thenReturn(predicate);

        when(cb.lessThanOrEqualTo(trainingDatePath, LocalDate.of(2026, 5, 31)))
                .thenReturn(predicate);
    }

    private void mockCriteriaNameLikeForTrainer() {
        when(trainerUserJoin.<String>get("firstName")).thenReturn(firstNamePath);
        when(trainerUserJoin.<String>get("lastName")).thenReturn(lastNamePath);

        mockFullNameLikeExpression("%trainer%");
    }

    private void mockCriteriaNameLikeForTrainee() {
        when(traineeUserJoin.<String>get("firstName")).thenReturn(firstNamePath);
        when(traineeUserJoin.<String>get("lastName")).thenReturn(lastNamePath);

        mockFullNameLikeExpression("%john%");
    }

    private void mockFullNameLikeExpression(String pattern) {
        when(cb.concat(firstNamePath, " ")).thenReturn(firstNameWithSpaceExpression);
        when(cb.concat(firstNameWithSpaceExpression, lastNamePath)).thenReturn(fullNameExpression);
        when(cb.lower(fullNameExpression)).thenReturn(lowerFullNameExpression);
        when(cb.like(lowerFullNameExpression, pattern)).thenReturn(predicate);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockCriteriaFinalQuery(List<Training> result) {
        doReturn(trainingDatePath)
                .when(trainingRoot)
                .get("trainingDate");

        when(cb.desc(trainingDatePath)).thenReturn(order);

        when(cq.select(trainingRoot)).thenReturn(cq);
        when(cq.where(any(Predicate[].class))).thenReturn(cq);
        when(cq.orderBy(order)).thenReturn(cq);

        when(em.createQuery(cq)).thenReturn(trainingQuery);
        when(trainingQuery.getResultList()).thenReturn(result);
    }

    private Training createTraining() {
        return Training.builder()
                .id(1L)
                .trainee(createTrainee(1L, "John.Smith"))
                .trainer(createTrainer(1L, "Trainer.One"))
                .trainingType(createTrainingType(1L, "Cardio"))
                .trainingName("Morning Cardio")
                .trainingDate(LocalDate.of(2026, 5, 5))
                .trainingDuration(60)
                .build();
    }

    private Trainee createTrainee(Long id, String username) {
        return Trainee.builder()
                .id(id)
                .user(createUser(id, username))
                .dateOfBirth(LocalDate.of(2000, 5, 10))
                .address("London")
                .build();
    }

    private Trainer createTrainer(Long id, String username) {
        return Trainer.builder()
                .id(id)
                .user(createUser(id + 100, username))
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