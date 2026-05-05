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
@Table(name = "training_types")
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String trainingTypeName;

    @OneToMany(mappedBy = "trainingType", fetch = FetchType.LAZY)
    private Set<Training> trainings = new HashSet<>();

    @OneToMany(mappedBy = "specialization", fetch = FetchType.LAZY)
    private Set<Trainer> trainers = new HashSet<>();
}
