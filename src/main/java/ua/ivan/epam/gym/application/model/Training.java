package ua.ivan.epam.gym.application.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Training {
    private Long id;

    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private Long trainingTypeId;
    private LocalDate trainingDate;
    private Integer trainingDuration;
}