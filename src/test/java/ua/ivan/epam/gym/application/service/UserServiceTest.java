package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void changePasswordShouldChangePasswordWhenOldPasswordIsCorrect() {
        User user = createUser("John.Smith", "encodedOldPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(
                "John.Smith",
                "oldPassword",
                "newPassword"
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("oldPassword", "encodedOldPassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encodedNewPassword");

        assertDoesNotThrow(() -> userService.changePassword(request));

        assertEquals("encodedNewPassword", user.getPassword());

        verify(userRepository).findByUsername("John.Smith");
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword");
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
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void changePasswordShouldThrowAuthenticationExceptionWhenOldPasswordIsIncorrect() {
        User user = createUser("John.Smith", "encodedCorrectOldPassword");

        ChangePasswordRequest request = new ChangePasswordRequest(
                "John.Smith",
                "wrongOldPassword",
                "newPassword"
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrongOldPassword", "encodedCorrectOldPassword"))
                .thenReturn(false);

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> userService.changePassword(request)
        );

        assertEquals("Invalid username or password", exception.getMessage());
        assertEquals("encodedCorrectOldPassword", user.getPassword());

        verify(userRepository).findByUsername("John.Smith");
        verify(passwordEncoder).matches("wrongOldPassword", "encodedCorrectOldPassword");
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void changePasswordShouldAllowSameNewPasswordAsOldPassword() {
        User user = createUser("John.Smith", "encodedSamePassword");

        ChangePasswordRequest request = new ChangePasswordRequest(
                "John.Smith",
                "samePassword",
                "samePassword"
        );

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("samePassword", "encodedSamePassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("samePassword"))
                .thenReturn("newEncodedSamePassword");

        assertDoesNotThrow(() -> userService.changePassword(request));

        assertEquals("newEncodedSamePassword", user.getPassword());

        verify(userRepository).findByUsername("John.Smith");
        verify(passwordEncoder).matches("samePassword", "encodedSamePassword");
        verify(passwordEncoder).encode("samePassword");
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