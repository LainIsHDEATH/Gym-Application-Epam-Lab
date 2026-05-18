package ua.ivan.epam.gym.application.authentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.exception.exceptions.AuthenticationException;
import ua.ivan.epam.gym.application.model.User;
import ua.ivan.epam.gym.application.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateShouldPassWhenUsernameAndPasswordMatch() {
        User user = createUser("John.Smith", "password12");

        when(userRepo.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        assertDoesNotThrow(() ->
                authService.authenticate("John.Smith", "password12")
        );

        verify(userRepo).findByUsername("John.Smith");
    }

    @Test
    void authenticateShouldThrowAuthenticationExceptionWhenUserDoesNotExist() {
        when(userRepo.findByUsername("Unknown.User"))
                .thenReturn(Optional.empty());

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.authenticate("Unknown.User", "password12")
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepo).findByUsername("Unknown.User");
    }

    @Test
    void authenticateShouldThrowAuthenticationExceptionWhenPasswordDoesNotMatch() {
        User user = createUser("John.Smith", "correctPassword");

        when(userRepo.findByUsername("John.Smith"))
                .thenReturn(Optional.of(user));

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.authenticate("John.Smith", "wrongPassword")
        );

        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepo).findByUsername("John.Smith");
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