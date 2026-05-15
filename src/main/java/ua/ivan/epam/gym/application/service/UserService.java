package ua.ivan.epam.gym.application.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.ivan.epam.gym.application.dto.request.ChangePasswordRequest;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = request.username();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found. username={}", username);
                    return new EntityNotFoundException("User not found. username=" + username);
                });

        if (!request.oldPassword().equals(user.getPassword())) {
            log.warn("Cannot change user password. Old password is incorrect. username={}", username);
            throw new IllegalArgumentException("Old password is incorrect");
        }
        user.setPassword(request.newPassword());

        log.info("Changed trainer password. username={}", username);
    }
}
