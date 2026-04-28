package ua.ivan.epam.gym.application.utils;

import org.springframework.stereotype.Component;
import ua.ivan.epam.gym.application.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UsernameGenerator {

    public String generate(String firstName, String lastName, List<User> existingUsers) {
        String base = firstName + "." + lastName;

        Set<String> usernames = existingUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

        if (!usernames.contains(base)) {
            return base;
        }

        int i = 1;
        while (usernames.contains(base + i)) {
            i++;
        }

        return base + i;
    }
}
