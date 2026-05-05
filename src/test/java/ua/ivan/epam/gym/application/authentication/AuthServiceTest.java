package ua.ivan.epam.gym.application.authentication;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateShouldPassWhenUsernameAndPasswordMatch() {
        User user = createUser("John.Smith", "password12");

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() ->
                authService.authenticate("John.Smith", "password12")
        );

        verify(userRepository).findByUsername("John.Smith");
    }

    @Test
    void authenticateShouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> authService.authenticate("Unknown.User", "password12")
        );

        assertEquals("User not found", exception.getMessage());

        verify(userRepository).findByUsername("Unknown.User");
    }

    @Test
    void authenticateShouldThrowExceptionWhenPasswordDoesNotMatch() {
        User user = createUser("John.Smith", "correctPassword");

        when(userRepository.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.authenticate("John.Smith", "wrongPassword")
        );

        assertEquals("Invalid password", exception.getMessage());

        verify(userRepository).findByUsername("John.Smith");
    }

    private User createUser(String username, String password) {
        return User.builder()
                .id(1L)
                .firstName(username.split("\\.")[0])
                .lastName(username.split("\\.")[1])
                .username(username)
                .password(password)
                .isActive(true)
                .build();
    }
}