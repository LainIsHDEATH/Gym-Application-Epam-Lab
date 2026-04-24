package ua.ivan.epam.gym.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.Training;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TrainingDao implements CrudDao<Long, Training> {
    private static final Logger log = LoggerFactory.getLogger(TrainingDao.class);

    private Map<Long, Training> storage;
    private AtomicLong idGenerator;

    @Override
    public Training save(Training training) {
        long id = idGenerator.incrementAndGet();
        training.setId(id);
        storage.put(id, training);

        log.debug("Saved training. id={}", id);

        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Training> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Training update(Training training) {
        if (!storage.containsKey(training.getId())) {
            log.warn("Cannot update training. Training not found. id={}", training.getId());
            throw new RuntimeException("Training not found");
        }

        storage.put(training.getId(), training);

        log.debug("Updated training. id={}", training.getId());

        return training;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);

        log.debug("Deleted training. id={}", id);
    }

    @Autowired
    public void setStorage(Map<Long, Training> trainingStorage) {
        this.storage = trainingStorage;
    }

    @Autowired
    public void setIdGenerator(@Qualifier("trainingIdGenerator") AtomicLong trainingIdGenerator) {
        this.idGenerator = trainingIdGenerator;
    }
}
