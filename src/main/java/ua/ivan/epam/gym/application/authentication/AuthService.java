package ua.ivan.epam.gym.application.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.exception.exceptions.AuthenticationException;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;

    public void authenticate (String username, String password) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("Invalid username or password");
        }
    }
}
