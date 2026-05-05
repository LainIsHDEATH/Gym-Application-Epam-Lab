package ua.ivan.epam.gym.application.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.User;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepository implements CrudRepo<Long, User> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public User save(User user) {
        em.persist(user);
        log.debug("Saved user. id={}, username={}", user.getId(), user.getUsername());

        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    public Optional<User> findByUsername(String username) {
        return em.createQuery("""
                        SELECT t
                        FROM User t
                        WHERE t.username = :username
                        """, User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    @Override
    public List<User> findAll() {
        return em.createQuery("""
                        SELECT t
                        FROM User t
                        """, User.class)
                .getResultList();
    }

    @Override
    public User update(User user) {
        if (user == null || user.getId() == null) {
            log.warn("Cannot update user. User or user id is null");
            throw new IllegalArgumentException("User and user id must not be null");
        }

        boolean userExists = existsById(user.getId());

        if (!userExists) {
            log.warn("Cannot update user. User not found. id={}", user.getId());
            throw new EntityNotFoundException("User not found. id=" + user.getId());
        }

        User updatedUser = em.merge(user);

        log.debug("Updated user. id={}", updatedUser.getId());

        return updatedUser;
    }

    @Override
    public void deleteById(Long id) {
        User user = em.find(User.class, id);

        try {
            em.remove(user);
            log.debug("Deleted user. id={}", id);
        } catch (IllegalArgumentException e) {
            log.warn("Cannot delete user. User not found. id={}", id);
        }
    }

    public boolean existsById(Long id) {
        return em.createQuery("""
                SELECT count(t)
                FROM User t
                WHERE t.id = :id
                """, Long.class)
                .setParameter("id", id)
                .getSingleResult() > 0;
    }
}
