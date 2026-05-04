package ua.ivan.epam.gym.application.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.dao.UserDao;
import ua.ivan.epam.gym.application.model.User;

@Component
public class AuthService {

    private final UserDao userDao;

    @Autowired
    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public void authenticate (String username, String password) {
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
    }
}
