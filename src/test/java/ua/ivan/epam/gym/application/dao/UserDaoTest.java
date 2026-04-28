package ua.ivan.epam.gym.application.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.ivan.epam.gym.application.model.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class UserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new UserDao();
        userDao.setStorage(new ConcurrentHashMap<>());
        userDao.setIdGenerator(new AtomicLong(0));
    }

    @Test
    void saveShouldAssignIdAndStoreUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Smith");
        user.setUsername("John.Smith");
        user.setPassword("password12");
        user.setIsActive(true);

        User saved = userDao.save(user);

        assertEquals(1L, saved.getId());
        assertEquals("John", saved.getFirstName());
        assertEquals("Smith", saved.getLastName());
        assertEquals("John.Smith", saved.getUsername());
        assertEquals("password12", saved.getPassword());
        assertTrue(saved.getIsActive());
    }

    @Test
    void findByIdShouldReturnUserWhenExists() {
        User saved = userDao.save(new User());

        assertTrue(userDao.findById(saved.getId()).isPresent());
    }

    @Test
    void findAllShouldReturnAllUsers() {
        User user1 = new User();
        user1.setUsername("John.Smith");

        User user2 = new User();
        user2.setUsername("Mike.Brown");

        userDao.save(user1);
        userDao.save(user2);

        assertEquals(2, userDao.findAll().size());
    }

    @Test
    void updateShouldUpdateExistingUser() {
        User saved = userDao.save(new User());
        saved.setUsername("Updated.Username");
        saved.setIsActive(false);

        User updated = userDao.update(saved);

        assertEquals("Updated.Username", updated.getUsername());
        assertFalse(updated.getIsActive());
    }

    @Test
    void updateShouldThrowExceptionWhenUserDoesNotExist() {
        User user = new User();
        user.setId(99L);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userDao.update(user)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void deleteByIdShouldRemoveUser() {
        User saved = userDao.save(new User());

        userDao.deleteById(saved.getId());

        assertTrue(userDao.findById(saved.getId()).isEmpty());
    }
}