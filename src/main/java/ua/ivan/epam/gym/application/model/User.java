package ua.ivan.epam.gym.application.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private Long id;

    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Boolean isActive;
}
