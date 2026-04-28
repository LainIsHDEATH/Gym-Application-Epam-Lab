package ua.ivan.epam.gym.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserDao implements CrudDao<Long, User> {
    private static final Logger log = LoggerFactory.getLogger(UserDao.class);

    private Map<Long, User> storage;
    private AtomicLong idGenerator;

    @Override
    public User save(User user) {
        long id = idGenerator.incrementAndGet();
        user.setId(id);
        storage.put(id, user);

        log.debug("Saved user. id={}, username={}", id, user.getUsername());

        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public User update(User user) {
        if (!storage.containsKey(user.getId())) {
            log.warn("Cannot update user. User not found. id={}", user.getId());
            throw new RuntimeException("User not found");
        }

        storage.put(user.getId(), user);

        log.debug("Updated user. id={}, username={}", user.getId(), user.getUsername());

        return user;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);

        log.debug("Deleted user. id={}", id);
    }

    @Autowired
    public void setStorage(Map<Long, User> userStorage) {
        this.storage = userStorage;
    }

    @Autowired
    public void setIdGenerator(@Qualifier("userIdGenerator") AtomicLong userIdGenerator) {
        this.idGenerator = userIdGenerator;
    }
}
