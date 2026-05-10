package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.Trainee;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class TraineeRepository implements CrudRepo<Long, Trainee> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Trainee save(Trainee trainee) {
        em.persist(trainee);
        log.debug("Saved trainee. id={}", trainee.getId());
        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(em.find(Trainee.class, id));
    }

    public Optional<Trainee> findByUsername(String username) {
        return em.createQuery("""
                SELECT t
                FROM Trainee t
                JOIN FETCH t.user u
                WHERE u.username = :username
                """, Trainee.class)
                .setParameter("username", username)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<Trainee> findAll() {
        return em.createQuery("""
                        SELECT t
                        FROM Trainee t
                        """, Trainee.class)
                .getResultList();
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (trainee == null || trainee.getId() == null) {
            log.warn("Cannot update trainee. Trainee or trainee id is null");
            throw new IllegalArgumentException("Trainee and trainee id must not be null");
        }

        boolean traineeExists = existsById(trainee.getId());

        if (!traineeExists) {
            log.warn("Cannot update trainee. Trainee not found. id={}", trainee.getId());
            throw new EntityNotFoundException("Trainee not found. id=" + trainee.getId());
        }

        Trainee updatedTrainee = em.merge(trainee);

        log.debug("Updated trainee. id={}", updatedTrainee.getId());

        return updatedTrainee;
    }

    @Override
    public void deleteById (Long id){
        Trainee trainee = em.find(Trainee.class, id);

        try {
            em.remove(trainee);
            log.debug("Deleted trainee. id={}", id);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete trainee. Trainee not found. id={}", id);
        }
    }

    public boolean existsById(Long id) {
        return em.createQuery("""
                SELECT count(t)
                FROM Trainee t
                WHERE t.id = :id
                """, Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }
}
