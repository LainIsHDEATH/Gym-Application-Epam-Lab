package ua.ivan.epam.gym.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.Trainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TrainerDao implements CrudDao<Long, Trainer> {
    private static final Logger log = LoggerFactory.getLogger(TrainerDao.class);

    private Map<Long, Trainer> storage;
    private AtomicLong idGenerator;

    @Override
    public Trainer save(Trainer trainer) {
        long id = idGenerator.incrementAndGet();
        trainer.setId(id);
        storage.put(id, trainer);

        log.debug("Saved trainer. id={}", id);

        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainer> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Trainer update(Trainer trainer) {
        if (!storage.containsKey(trainer.getId())) {
            log.warn("Cannot update trainer. Trainer not found. id={}", trainer.getId());
            throw new RuntimeException("Trainer not found");
        }

        storage.put(trainer.getId(), trainer);

        log.debug("Updated trainer. id={}", trainer.getId());

        return trainer;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);

        log.debug("Deleted trainer. id={}", id);
    }

    @Autowired
    public void setStorage(Map<Long, Trainer> trainerStorage) {
        this.storage = trainerStorage;
    }

    @Autowired
    public void setIdGenerator(@Qualifier("trainerIdGenerator") AtomicLong trainerIdGenerator) {
        this.idGenerator = trainerIdGenerator;
    }
}
