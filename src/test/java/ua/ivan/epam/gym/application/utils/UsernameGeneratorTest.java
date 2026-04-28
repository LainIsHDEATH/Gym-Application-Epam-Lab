package ua.ivan.epam.gym.application.utils;

import org.junit.jupiter.api.Test;
import ua.ivan.epam.gym.application.model.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameGeneratorTest {

    private final UsernameGenerator usernameGenerator = new UsernameGenerator();

    @Test
    void generateShouldReturnFirstNameDotLastNameWhenUsernameDoesNotExist() {
        String username = usernameGenerator.generate(
                "John",
                "Smith",
                List.of()
        );

        assertEquals("John.Smith", username);
    }

    @Test
    void generateShouldAddSerialNumberWhenUsernameAlreadyExists() {
        User user1 = user("John", "Smith", "John.Smith");
        User user2 = user("John", "Smith", "John.Smith1");

        String username = usernameGenerator.generate(
                "John",
                "Smith",
                List.of(user1, user2)
        );

        assertEquals("John.Smith2", username);
    }

    private User user(String firstName, String lastName, String username) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        return user;
    }
}