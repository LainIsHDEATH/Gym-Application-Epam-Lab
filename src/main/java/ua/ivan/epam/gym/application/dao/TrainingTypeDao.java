package ua.ivan.epam.gym.application.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import ua.ivan.epam.gym.application.model.TrainingType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class TrainingTypeDao implements CrudDao<Long, TrainingType> {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDao.class);

    private Map<Long, TrainingType> storage;
    private AtomicLong idGenerator;

    public TrainingType save(TrainingType trainingType) {
        long id = idGenerator.incrementAndGet();
        trainingType.setId(id);
        storage.put(id, trainingType);

        log.debug("Saved training type. id={}, name={}",
                id, trainingType.getTrainingTypeName());

        return trainingType;
    }

    @Override
    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<TrainingType> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public TrainingType update(TrainingType trainingType) {
        if (!storage.containsKey(trainingType.getId())) {
            log.warn("Cannot update training type. Training type not found. id={}",
                    trainingType.getId());
            throw new RuntimeException("Training type not found");
        }

        storage.put(trainingType.getId(), trainingType);

        log.debug("Updated training type. id={}, name={}",
                trainingType.getId(), trainingType.getTrainingTypeName());

        return trainingType;
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);

        log.debug("Deleted training type. id={}", id);
    }

    @Autowired
    public void setStorage(Map<Long, TrainingType> trainingTypeStorage) {
        this.storage = trainingTypeStorage;
    }

    @Autowired
    public void setIdGenerator(@Qualifier("trainingTypeIdGenerator") AtomicLong trainingTypeIdGenerator) {
        this.idGenerator = trainingTypeIdGenerator;
    }
}
