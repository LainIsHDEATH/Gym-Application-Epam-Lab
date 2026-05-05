package ua.ivan.epam.gym.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "trainees")
public class Trainee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "address")
    private String address;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
//            foreignKey = @ForeignKey(name = "fk_trainees_users")
    )
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "trainee_trainers",
            joinColumns = @JoinColumn(
                    name = "trainee_id"
//                    foreignKey = @ForeignKey(name = "fk_trainee_trainers_trainees")
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "trainer_id"
//                    foreignKey = @ForeignKey(name = "fk_trainee_trainers_trainers")
            )
//            uniqueConstraints = {
//                    @UniqueConstraint(
//                            name = "uk_trainee_trainers_pair",
//                            columnNames = {"trainee_id", "trainer_id"}
//                    )
//            }
    )
    private Set<Trainer> trainers = new HashSet<>();

    @OneToMany(
            mappedBy = "trainee",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Training> trainings = new HashSet<>();

    public void addTrainer(Trainer trainer) {
        trainers.add(trainer);
        trainer.getTrainees().add(this);
    }

    public void removeTrainer(Trainer trainer) {
        trainers.remove(trainer);
        trainer.getTrainees().remove(this);
    }
}
