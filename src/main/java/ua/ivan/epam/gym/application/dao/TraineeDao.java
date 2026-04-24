package ua.ivan.epam.gym.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.Trainee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TraineeDao implements CrudDao<Long, Trainee> {
    private static final Logger log = LoggerFactory.getLogger(TraineeDao.class);

    private Map<Long, Trainee> storage;
    private AtomicLong idGenerator;

    @Override
    public Trainee save(Trainee trainee) {
        long id = idGenerator.incrementAndGet();
        trainee.setId(id);
        storage.put(id, trainee);

        log.debug("Saved trainee. id={}", id);

        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Trainee update(Trainee trainee) {
        if (!storage.containsKey(trainee.getId())) {
            log.warn("Cannot update trainee. Trainee not found. id={}", trainee.getId());
            throw new RuntimeException("Trainee not found");
        }

        storage.put(trainee.getId(), trainee);

        log.debug("Updated trainee. id={}", trainee.getId());

        return trainee;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);

        log.debug("Deleted trainee. id={}", id);
    }

    @Autowired
    public void setStorage(Map<Long, Trainee> traineeStorage) {
        this.storage = traineeStorage;
    }

    @Autowired
    public void setIdGenerator(@Qualifier("traineeIdGenerator") AtomicLong traineeIdGenerator) {
        this.idGenerator = traineeIdGenerator;
    }
}
