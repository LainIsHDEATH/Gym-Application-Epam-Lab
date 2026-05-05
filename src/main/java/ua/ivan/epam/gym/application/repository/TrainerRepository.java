package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.Trainer;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TrainerRepository implements CrudRepo<Long, Trainer> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Trainer save(Trainer trainer) {
        em.persist(trainer);
        log.debug("Saved trainer. id={}", trainer.getId());
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(em.find(Trainer.class, id));
    }

    public Optional<Trainer> findByUsername(String username) {
        return em.createQuery("""
                SELECT t
                FROM Trainer t
                JOIN FETCH t.user u
                JOIN FETCH t.specialization
                WHERE u.username = :username
                """, Trainer.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Trainer> findAll() {
        return em.createQuery("""
                        SELECT t
                        FROM Trainer t
                        """, Trainer.class)
                .getResultList();
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (trainer == null || trainer.getId() == null) {
            log.warn("Cannot update trainer. Trainer or trainer id is null");
            throw new IllegalArgumentException("Trainer and trainer id must not be null");
        }

        boolean trainerExists = existsById(trainer.getId());

        if (!trainerExists) {
            log.warn("Cannot update trainer. Trainer not found. id={}", trainer.getId());
            throw new EntityNotFoundException("Trainer not found. id=" + trainer.getId());
        }

        Trainer updatedTrainer = em.merge(trainer);

        log.debug("Updated trainer. id={}", updatedTrainer.getId());

        return updatedTrainer;
    }

    @Override
    public void deleteById(Long id) {
        Trainer trainer = em.find(Trainer.class, id);

        try {
            em.remove(trainer);
            log.debug("Deleted trainer. id={}", id);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete trainer. Trainer not found. id={}", id);
        }
    }

    public boolean existsById(Long id) {
        return em.createQuery("""
                        SELECT count(t)
                        FROM Trainer t
                        WHERE t.id = :id
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }

    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        return em.createQuery("""
            SELECT tr
            FROM Trainer tr
            JOIN FETCH tr.user u
            WHERE tr.id NOT IN (
                SELECT assignedTrainer.id
                FROM Trainee te
                JOIN te.user traineeUser
                JOIN te.trainers assignedTrainer
                WHERE traineeUser.username = :traineeUsername
            )
            """, Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }
}