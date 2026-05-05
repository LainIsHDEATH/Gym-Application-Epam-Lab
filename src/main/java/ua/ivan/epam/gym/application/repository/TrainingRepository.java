package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.Training;

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
}
