package ua.ivan.epam.gym.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.ivan.epam.gym.application.dao.CrudDao;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.utils.PasswordGenerator;
import ua.ivan.epam.gym.application.utils.UsernameGenerator;

import java.util.List;

@Service
public class TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerService.class);

    private final CrudDao<Long, Trainer> trainerDao;
    private final CrudDao<Long, User> userDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    @Autowired
    public TrainerService(CrudDao<Long, Trainer> trainerDao,
                          CrudDao<Long, User> userDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator) {
        this.trainerDao = trainerDao;
        this.userDao = userDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    public Trainer create(String firstName, String lastName,
                          String specialization) {
        log.info("Creating trainer profile for {} {}, specialization={}",
                firstName, lastName, specialization);

        List<User> users = userDao.findAll();

        String username = usernameGenerator.generate(firstName, lastName, users);
        String password = passwordGenerator.generate();

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(password);
        user.setIsActive(true);

        User savedUser = userDao.save(user);

        Trainer trainer = new Trainer();
        trainer.setUserId(savedUser.getId());
        trainer.setSpecialization(specialization);
        Trainer savedTrainer = trainerDao.save(trainer);

        log.info("Created trainer profile. trainerId={}, userId={}, username={}",
                savedTrainer.getId(), savedUser.getId(), savedUser.getUsername());

        return savedTrainer;
    }

    public Trainer get(Long id) {
        log.debug("Searching trainer by id={}", id);

        return trainerDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainer not found. id={}", id);
                    return new RuntimeException("Trainer not found");
                });
    }

    public Trainer update(Trainer trainer) {
        log.info("Updating trainer profile. trainerId={}", trainer.getId());

        Trainer updated = trainerDao.update(trainer);

        log.info("Updated trainer profile. trainerId={}", updated.getId());

        return updated;
    }
}
