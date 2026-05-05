package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TrainingRepository implements CrudRepo<Long, Training> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Training save(Training training) {
        em.persist(training);
        log.debug("Saved training. id={}", training.getId());
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(em.find(Training.class, id));
    }

    @Override
    public List<Training> findAll() {
        return em.createQuery("""
                        SELECT t
                        FROM Training t
                        """, Training.class)
                .getResultList();
    }

    @Override
    public Training update(Training training) {
        if (training == null || training.getId() == null) {
            log.warn("Cannot update training. Training or training id is null");
            throw new IllegalArgumentException("Training and training id must not be null");
        }

        boolean trainingExists = existsById(training.getId());

        if (!trainingExists) {
            log.warn("Cannot update training. Training not found. id={}", training.getId());
            throw new EntityNotFoundException("Training not found. id=" + training.getId());
        }

        Training updatedTraining = em.merge(training);

        log.debug("Updated training. id={}", updatedTraining.getId());

        return updatedTraining;
    }

    @Override
    public void deleteById(Long id) {
        Training training = em.find(Training.class, id);

        try {
            em.remove(training);
            log.debug("Deleted training. id={}", id);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete training. Training not found. id={}", id);
        }
    }

    public boolean existsById(Long id) {
        return em.createQuery("""
                        SELECT count(t)
                        FROM Training t
                        WHERE t.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    public List<Training> findTraineeTrainingsByCriteria(String traineeUsername,
                                                         LocalDate fromDate,
                                                         LocalDate toDate,
                                                         String trainerName,
                                                         Long trainingTypeId) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Training.class);

        var training = cq.from(Training.class);

        var trainee = training.join("trainee", JoinType.INNER);
        var traineeUser = trainee.join("user", JoinType.INNER);

        var trainer = training.join("trainer", JoinType.INNER);
        var trainerUser = trainer.join("user", JoinType.INNER);

        var trainingType = training.join("trainingType", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(traineeUser.get("username"), traineeUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (trainerName != null && !trainerName.isBlank()) {
            String pattern = "%" + trainerName.trim().toLowerCase() + "%";

            predicates.add(cb.like(
                    cb.lower(
                            cb.concat(
                                    cb.concat(trainerUser.get("firstName"), " "),
                                    trainerUser.get("lastName")
                            )
                    ),
                    pattern
            ));
        }

        if (trainingTypeId != null) {
            predicates.add(cb.equal(trainingType.get("id"), trainingTypeId));
        }

        cq.select(training)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(training.get("trainingDate")));

        return em.createQuery(cq).getResultList();
    }

    public List<Training> findTrainerTrainingsByCriteria(String trainerUsername,
                                                         LocalDate fromDate,
                                                         LocalDate toDate,
                                                         String traineeName) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Training.class);

        var training = cq.from(Training.class);

        var trainer = training.join("trainer", JoinType.INNER);
        var trainerUser = trainer.join("user", JoinType.INNER);

        var trainee = training.join("trainee", JoinType.INNER);
        var traineeUser = trainee.join("user", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(trainerUser.get("username"), trainerUsername));

        if (fromDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }

        if (toDate != null) {
            predicates.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }

        if (traineeName != null && !traineeName.isBlank()) {
            String pattern = "%" + traineeName.trim().toLowerCase() + "%";

            predicates.add(cb.like(
                    cb.lower(
                            cb.concat(
                                    cb.concat(traineeUser.get("firstName"), " "),
                                    traineeUser.get("lastName")
                            )
                    ),
                    pattern
            ));
        }

        cq.select(training)
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(training.get("trainingDate")));

        return em.createQuery(cq).getResultList();
    }
}
