package ua.ivan.epam.gym.application.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Trainee {
    private Long id;

    private Long userId;
    private LocalDate dateOfBirth;
    private String address;
}
