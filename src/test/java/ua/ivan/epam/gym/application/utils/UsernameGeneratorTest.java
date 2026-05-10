package ua.ivan.epam.gym.application.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsernameGeneratorTest {

    private final UsernameGenerator usernameGenerator = new UsernameGenerator();

    @Test
    void generateShouldReturnBaseUsernameWhenUsernameDoesNotExist() {
        Predicate<String> usernameExists = username -> false;

        String result = usernameGenerator.generate(
                "John",
                "Smith",
                usernameExists
        );

        assertEquals("John.Smith", result);
    }

    @Test
    void generateShouldAddSuffixOneWhenBaseUsernameExists() {
        Set<String> existingUsernames = Set.of(
                "John.Smith"
        );

        String result = usernameGenerator.generate(
                "John",
                "Smith",
                existingUsernames::contains
        );

        assertEquals("John.Smith1", result);
    }

    @Test
    void generateShouldFindNextAvailableSuffixWhenSeveralUsernamesExist() {
        Set<String> existingUsernames = Set.of(
                "John.Smith",
                "John.Smith1",
                "John.Smith2",
                "John.Smith3"
        );

        String result = usernameGenerator.generate(
                "John",
                "Smith",
                existingUsernames::contains
        );

        assertEquals("John.Smith4", result);
    }

    @Test
    void generateShouldReturnFirstFreeSuffixWhenThereIsGap() {
        Set<String> existingUsernames = Set.of(
                "John.Smith",
                "John.Smith1",
                "John.Smith3"
        );

        String result = usernameGenerator.generate(
                "John",
                "Smith",
                existingUsernames::contains
        );

        assertEquals("John.Smith2", result);
    }
}