package ua.ivan.epam.gym.application.utils;

import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.model.User;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class UsernameGenerator {

    public String generate(String firstName, String lastName, Predicate<String> usernameExists) {
        String base = firstName + "." + lastName;
        String candidate = base;
        int suffix = 1;

        while (usernameExists.test(candidate)) {
            candidate = base + suffix;
            suffix++;
        }

        return candidate;
    }
}
