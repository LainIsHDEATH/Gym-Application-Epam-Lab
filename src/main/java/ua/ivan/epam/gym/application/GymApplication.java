package ua.ivan.epam.gym.application;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ua.ivan.epam.gym.application.config.AppConfig;
import ua.ivan.epam.gym.application.dto.ChangePasswordRequest;
import ua.ivan.epam.gym.application.dto.CreateTraineeRequest;
import ua.ivan.epam.gym.application.dto.CreateTrainerRequest;
import ua.ivan.epam.gym.application.dto.CreateTrainingRequest;
import ua.ivan.epam.gym.application.dto.GetTraineeTrainingsRequest;
import ua.ivan.epam.gym.application.dto.GetTrainerTrainingsRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeRequest;
import ua.ivan.epam.gym.application.dto.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.dto.UpdateTrainerRequest;
import ua.ivan.epam.gym.application.facade.GymFacade;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;

import java.time.LocalDate;
import java.util.List;

public class GymApplication {

    public static void main(String[] args) {

        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(AppConfig.class)) {

            GymFacade facade = context.getBean(GymFacade.class);

            System.out.println("=== APPLICATION STARTED ===");

            // =========================
            // 1. Create Trainee profile
            // =========================

            Trainee trainee = facade.createTrainee(new CreateTraineeRequest(
                    "John",
                    "Smith",
                    LocalDate.of(2000, 5, 10),
                    "London"
            ));

            String traineeUsername = trainee.getUser().getUsername();
            String traineePassword = trainee.getUser().getPassword();

            System.out.println("\nCreated trainee:");
            System.out.println("traineeId=" + trainee.getId());
            System.out.println("username=" + traineeUsername);
            System.out.println("password=" + traineePassword);

            // =========================
            // 2. Create Trainer profile
            // =========================
            // Assumption: TrainingType with id=1 exists in DB.

            Trainer trainer = facade.createTrainer(new CreateTrainerRequest(
                    "Mike",
                    "Brown",
                    1L
            ));

            String trainerUsername = trainer.getUser().getUsername();
            String trainerPassword = trainer.getUser().getPassword();

            System.out.println("\nCreated trainer:");
            System.out.println("trainerId=" + trainer.getId());
            System.out.println("username=" + trainerUsername);
            System.out.println("password=" + trainerPassword);

            // =========================
            // Create second trainer for not-assigned/update-trainers-list demo
            // =========================

            Trainer secondTrainer = facade.createTrainer(new CreateTrainerRequest(
                    "Alice",
                    "Johnson",
                    1L
            ));

            String secondTrainerUsername = secondTrainer.getUser().getUsername();
            String secondTrainerPassword = secondTrainer.getUser().getPassword();

            System.out.println("\nCreated second trainer:");
            System.out.println("trainerId=" + secondTrainer.getId());
            System.out.println("username=" + secondTrainerUsername);
            System.out.println("password=" + secondTrainerPassword);

            // =========================
            // 3/4. Username and password matching
            // =========================
            // Your facade currently exposes authentication indirectly through protected methods.
            // If auth fails, these calls throw exception.

            System.out.println("\nAuthentication check via protected get methods:");

            Trainee loadedTrainee = facade.getTraineeByUsername(
                    traineeUsername,
                    traineeUsername,
                    traineePassword
            );

            Trainer loadedTrainer = facade.getTrainerByUsername(
                    trainerUsername,
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("Authenticated trainee and loaded profile. id=" + loadedTrainee.getId());
            System.out.println("Authenticated trainer and loaded profile. id=" + loadedTrainer.getId());

            // =========================
            // 5. Select Trainer profile by username
            // =========================

            Trainer trainerByUsername = facade.getTrainerByUsername(
                    trainerUsername,
                    traineeUsername,
                    traineePassword
            );

            System.out.println("\nTrainer by username:");
            System.out.println("id=" + trainerByUsername.getId());
            System.out.println("username=" + trainerByUsername.getUser().getUsername());

            // =========================
            // 6. Select Trainee profile by username
            // =========================

            Trainee traineeByUsername = facade.getTraineeByUsername(
                    traineeUsername,
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("\nTrainee by username:");
            System.out.println("id=" + traineeByUsername.getId());
            System.out.println("username=" + traineeByUsername.getUser().getUsername());

            // =========================
            // 7. Trainee password change
            // =========================

            String newTraineePassword = "newTraineePassword123";

            facade.changeTraineePassword(new ChangePasswordRequest(
                    traineeUsername,
                    traineePassword,
                    newTraineePassword
            ));

            traineePassword = newTraineePassword;

            System.out.println("\nChanged trainee password.");

            // Check new trainee password works

            facade.getTraineeByUsername(
                    traineeUsername,
                    traineeUsername,
                    traineePassword
            );

            System.out.println("New trainee password works.");

            // =========================
            // 8. Trainer password change
            // =========================

            String newTrainerPassword = "newTrainerPassword123";

            facade.changeTrainerPassword(new ChangePasswordRequest(
                    trainerUsername,
                    trainerPassword,
                    newTrainerPassword
            ));

            trainerPassword = newTrainerPassword;

            System.out.println("\nChanged trainer password.");

            // Check new trainer password works

            facade.getTrainerByUsername(
                    trainerUsername,
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("New trainer password works.");

            // =========================
            // 9. Update trainer profile
            // =========================
            // Current UpdateTrainerRequest updates specialization only.
            // Assumption: TrainingType with id=1 exists.

            Trainer updatedTrainer = facade.updateTrainer(
                    new UpdateTrainerRequest(
                            trainer.getId(),
                            1L
                    ),
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("\nUpdated trainer:");
            System.out.println("trainerId=" + updatedTrainer.getId());
            System.out.println("specializationId=" + updatedTrainer.getSpecialization().getId());

            // =========================
            // 10. Update trainee profile
            // =========================
            // Current UpdateTraineeRequest updates dateOfBirth/address.

            Trainee updatedTrainee = facade.updateTrainee(
                    new UpdateTraineeRequest(
                            trainee.getId(),
                            LocalDate.of(2001, 1, 15),
                            "Berlin"
                    ),
                    traineeUsername,
                    traineePassword
            );

            System.out.println("\nUpdated trainee:");
            System.out.println("traineeId=" + updatedTrainee.getId());
            System.out.println("dateOfBirth=" + updatedTrainee.getDateOfBirth());
            System.out.println("address=" + updatedTrainee.getAddress());

            // =========================
            // 16. Add training
            // =========================
            // Assumption: TrainingType with id=1 exists.

            Training training = facade.addTraining(
                    new CreateTrainingRequest(
                            trainee.getId(),
                            trainer.getId(),
                            "Morning Cardio",
                            1L,
                            LocalDate.now(),
                            60
                    ),
                    traineeUsername,
                    traineePassword
            );

            System.out.println("\nAdded training:");
            System.out.println("trainingId=" + training.getId());
            System.out.println("name=" + training.getTrainingName());
            System.out.println("date=" + training.getTrainingDate());
            System.out.println("duration=" + training.getTrainingDuration());

            // =========================
            // 14. Get Trainee Trainings List by criteria
            // =========================

            List<Training> traineeTrainings = facade.getTraineeTrainings(
                    new GetTraineeTrainingsRequest(
                            traineeUsername,
                            LocalDate.now().minusDays(7),
                            LocalDate.now().plusDays(7),
                            "Mike",
                            1L
                    ),
                    traineeUsername,
                    traineePassword
            );

            System.out.println("\nTrainee trainings by criteria:");
            traineeTrainings.forEach(t ->
                    System.out.println("trainingId=" + t.getId()
                            + ", name=" + t.getTrainingName()
                            + ", date=" + t.getTrainingDate())
            );

            // =========================
            // 15. Get Trainer Trainings List by criteria
            // =========================

            List<Training> trainerTrainings = facade.getTrainerTrainings(
                    new GetTrainerTrainingsRequest(
                            trainerUsername,
                            LocalDate.now().minusDays(7),
                            LocalDate.now().plusDays(7),
                            "John"
                    ),
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("\nTrainer trainings by criteria:");
            trainerTrainings.forEach(t ->
                    System.out.println("trainingId=" + t.getId()
                            + ", name=" + t.getTrainingName()
                            + ", date=" + t.getTrainingDate())
            );

            // =========================
            // 17. Get trainers list that not assigned on trainee
            // =========================

            List<Trainer> notAssignedTrainers = facade.getTrainersNotAssignedToTrainee(
                    traineeUsername,
                    traineeUsername,
                    traineePassword
            );

            System.out.println("\nTrainers not assigned to trainee:");
            notAssignedTrainers.forEach(t ->
                    System.out.println("trainerId=" + t.getId()
                            + ", username=" + t.getUser().getUsername())
            );

            // =========================
            // 18. Update Trainee's trainers list
            // =========================

            Trainee traineeWithUpdatedTrainers = facade.updateTraineeTrainersList(
                    new UpdateTraineeTrainersRequest(
                            traineeUsername,
                            List.of(trainerUsername, secondTrainerUsername)
                    ),
                    traineeUsername,
                    traineePassword
            );

            System.out.println("\nUpdated trainee trainers list:");
            System.out.println("traineeId=" + traineeWithUpdatedTrainers.getId());
            System.out.println("assignedTrainersCount=" + traineeWithUpdatedTrainers.getTrainers().size());

            // =========================
            // 11. Activate/De-activate trainee
            // =========================

            Trainee traineeAfterStatusChange = facade.changeTraineeStatus(
                    trainee.getId(),
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("\nChanged trainee status:");
            System.out.println("traineeId=" + traineeAfterStatusChange.getId());
            System.out.println("active=" + traineeAfterStatusChange.getUser().getIsActive());

            // Toggle back, so further auth is less likely to fail if AuthService checks active.

            traineeAfterStatusChange = facade.changeTraineeStatus(
                    trainee.getId(),
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("Changed trainee status back:");
            System.out.println("active=" + traineeAfterStatusChange.getUser().getIsActive());

            // =========================
            // 12. Activate/De-activate trainer
            // =========================

            Trainer trainerAfterStatusChange = facade.changeTrainerStatus(
                    trainer.getId(),
                    traineeUsername,
                    traineePassword
            );

            System.out.println("\nChanged trainer status:");
            System.out.println("trainerId=" + trainerAfterStatusChange.getId());
            System.out.println("active=" + trainerAfterStatusChange.getUser().getIsActive());

            // Toggle back, so delete/auth demo remains clean.

            trainerAfterStatusChange = facade.changeTrainerStatus(
                    trainer.getId(),
                    traineeUsername,
                    traineePassword
            );

            System.out.println("Changed trainer status back:");
            System.out.println("active=" + trainerAfterStatusChange.getUser().getIsActive());

            // =========================
            // 13. Delete trainee profile by username
            // =========================
            // This should hard-delete trainee and cascade related trainings
            // if your entity/DB cascade is configured correctly.

            facade.deleteTraineeByUsername(
                    traineeUsername,
                    trainerUsername,
                    trainerPassword
            );

            System.out.println("\nDeleted trainee by username:");
            System.out.println("username=" + traineeUsername);

            System.out.println("\n=== APPLICATION FINISHED ===");
        }
    }
}