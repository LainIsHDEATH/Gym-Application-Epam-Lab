package ua.ivan.epam.gym.application.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    void generateShouldReturnPasswordWithLengthTen() {
        String password = passwordGenerator.generate();

        assertNotNull(password);
        assertEquals(10, password.length());
    }

    @Test
    void generateShouldContainOnlyAllowedCharacters() {
        String password = passwordGenerator.generate();

        assertTrue(password.matches("[A-Za-z0-9]{10}"));
    }
}