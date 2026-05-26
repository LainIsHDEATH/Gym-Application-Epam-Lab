package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.request.ChangePasswordRequest;
import ua.ivan.epam.gym.application.exception.exceptions.AuthenticationException;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void changePasswordShouldChangePasswordWhenOldPasswordIsCorrect() {
        User user = createUser("John.Smith", "oldPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(
                "John.Smith",
                "oldPassword",
                "newPassword"
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.changePassword(request));

        assertEquals("newPassword", user.getPassword());

        verify(userRepository).findByUsername("John.Smith");
    }

    @Test
    void changePasswordShouldThrowAuthenticationExceptionWhenUserDoesNotExist() {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "Unknown.User",
                "oldPassword",
                "newPassword"
        );

        when(userRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> userService.changePassword(request)
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository).findByUsername("Unknown.User");
    }

    @Test
    void changePasswordShouldThrowAuthenticationExceptionWhenOldPasswordIsIncorrect() {
        User user = createUser("John.Smith", "correctOldPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(
                "John.Smith",
                "wrongOldPassword",
                "newPassword"
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> userService.changePassword(request)
        );

        assertEquals("Invalid username or password", exception.getMessage());
        assertEquals("correctOldPassword", user.getPassword());

        verify(userRepository).findByUsername("John.Smith");
    }

    @Test
    void changePasswordShouldAllowSameNewPasswordAsOldPassword() {
        User user = createUser("John.Smith", "samePassword");

        ChangePasswordRequest request = new ChangePasswordRequest(
                "John.Smith",
                "samePassword",
                "samePassword"
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.changePassword(request));

        assertEquals("samePassword", user.getPassword());

        verify(userRepository).findByUsername("John.Smith");
    }

    private User createUser(String username, String password) {
        String[] parts = username.split("\\.");

        return User.builder()
                .id(1L)
                .firstName(parts[0])
                .lastName(parts[1])
                .username(username)
                .password(password)
                .isActive(true)
                .build();
    }
}