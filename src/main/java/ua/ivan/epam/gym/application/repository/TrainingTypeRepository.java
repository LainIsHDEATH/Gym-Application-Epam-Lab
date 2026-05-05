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
public class TrainingTypeRepository {
    @PersistenceContext
    private EntityManager em;

    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(em.find(TrainingType.class, id));
    }

    public List<TrainingType> findAll() {
        return em.createQuery("""
                        SELECT t
                        FROM TrainingType t
                        """, TrainingType.class)
                .getResultList();
    }

    public Optional<TrainingType> findByName(String name) {
        return em.createQuery("""
                SELECT t
                FROM TrainingType t
                WHERE t.trainingTypeName = :name
                """, TrainingType.class)
                .setParameter("name", name)
                .getResultList()
                .stream()
                .findFirst();
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
