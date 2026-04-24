package ua.ivan.epam.gym.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.ivan.epam.gym.application.dao.CrudDao;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.utils.PasswordGenerator;
import ua.ivan.epam.gym.application.utils.UsernameGenerator;

import java.time.LocalDate;
import java.util.List;

@Service
public class TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeService.class);

    private final CrudDao<Long, Trainee> traineeDao;
    private final CrudDao<Long, User> userDao;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    @Autowired
    public TraineeService(CrudDao<Long, Trainee> traineeDao,
                          CrudDao<Long, User> userDao,
                          UsernameGenerator usernameGenerator,
                          PasswordGenerator passwordGenerator) {
        this.traineeDao = traineeDao;
        this.userDao = userDao;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    public Trainee create(String firstName, String lastName,
                          LocalDate dateOfBirth, String address) {
        log.info("Creating trainee profile for {} {}", firstName, lastName);

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

        Trainee trainee = new Trainee();
        trainee.setUserId(savedUser.getId());
        trainee.setDateOfBirth(dateOfBirth);
        trainee.setAddress(address);

        Trainee savedTrainee = traineeDao.save(trainee);

        log.info("Created trainee profile. traineeId={}, userId={}, username={}",
                savedTrainee.getId(), savedUser.getId(), savedUser.getUsername());

        return savedTrainee;
    }

    public Trainee get(Long id) {
        log.debug("Searching trainee by id={}", id);

        return traineeDao.findById(id)
                .orElseThrow(() -> {
                    log.warn("Trainee not found. id={}", id);
                    return new RuntimeException("Trainee not found");
                });
    }

    public Trainee update(Trainee trainee) {
        log.info("Updating trainee profile. traineeId={}", trainee.getId());

        Trainee updated = traineeDao.update(trainee);

        log.info("Updated trainee profile. traineeId={}", updated.getId());

        return updated;
    }

    public void delete(Long id) {
        log.info("Deleting trainee profile. traineeId={}", id);

        traineeDao.deleteById(id);

        log.info("Deleted trainee profile. traineeId={}", id);
    }
}