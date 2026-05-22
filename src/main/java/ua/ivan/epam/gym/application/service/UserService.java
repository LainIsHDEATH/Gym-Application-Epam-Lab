package ua.ivan.epam.gym.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.actuator.metrics.CountGymEvent;
import ua.ivan.epam.gym.application.actuator.metrics.GymMetric;
import ua.ivan.epam.gym.application.dto.request.ChangePasswordRequest;
import ua.ivan.epam.gym.application.exception.exceptions.AuthenticationException;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @CountGymEvent(GymMetric.PASSWORD_CHANGE)
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = request.username();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found. username={}", username);
                    return new AuthenticationException("Invalid username or password");
                });

        if (!request.oldPassword().equals(user.getPassword())) {
            log.warn("Cannot change user password. Old password is incorrect. username={}", username);
            throw new AuthenticationException("Invalid username or password");
        }
        user.setPassword(request.newPassword());

        log.info("Changed user password. username={}", username);
    }
}
