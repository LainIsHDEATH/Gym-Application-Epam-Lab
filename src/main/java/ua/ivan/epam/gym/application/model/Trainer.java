package ua.ivan.epam.gym.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trainers")
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "specialization_id",
            nullable = false
//            foreignKey = @ForeignKey(name = "fk_trainers_training_types")
    )
    private TrainingType specialization;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
//            foreignKey = @ForeignKey(name = "fk_trainers_users")
    )
    private User user;

    @ManyToMany(mappedBy = "trainers", fetch = FetchType.LAZY)
    private Set<Trainee> trainees = new HashSet<>();

    @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
    private Set<Training> trainings = new HashSet<>();
}
