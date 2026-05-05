package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.model.TrainingType;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class TrainingTypeRepository implements CrudRepo<Long, TrainingType> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public TrainingType save(TrainingType trainingType) {
        em.persist(trainingType);
        log.debug("Saved training type. id={}", trainingType.getId());
        return trainingType;
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(em.find(TrainingType.class, id));
    }

    @Override
    public List<TrainingType> findAll() {
        return em.createQuery("""
                        SELECT t
                        FROM TrainingType t
                        """, TrainingType.class)
                .getResultList();
    }

    @Override
    public TrainingType update(TrainingType trainingType) {
        if (trainingType == null || trainingType.getId() == null) {
            log.warn("Cannot update training type. Training type or training type id is null");
            throw new IllegalArgumentException("Training type and training type id must not be null");
        }

        boolean trainingExists = existsById(trainingType.getId());

        if (!trainingExists) {
            log.warn("Cannot update training type. Training type not found. id={}", trainingType.getId());
            throw new EntityNotFoundException("Training type not found. id=" + trainingType.getId());
        }

        TrainingType updatedTrainingType = em.merge(trainingType);

        log.debug("Updated training type. id={}", updatedTrainingType.getId());

        return updatedTrainingType;
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
                        FROM TrainingType t
                        WHERE t.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }
}
