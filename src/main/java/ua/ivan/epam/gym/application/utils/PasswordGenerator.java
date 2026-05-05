package ua.ivan.epam.gym.application.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PasswordGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private final Random random = new Random();

    public String generate() {
        StringBuilder sb = new StringBuilder(10);

        for (int i = 0; i < 10; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}