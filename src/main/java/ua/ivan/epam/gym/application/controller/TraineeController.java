package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.authentication.RequireAuth;
import ua.ivan.epam.gym.application.dto.request.*;
import ua.ivan.epam.gym.application.dto.response.*;
import ua.ivan.epam.gym.application.service.TraineeService;
import ua.ivan.epam.gym.application.service.TrainerService;
import ua.ivan.epam.gym.application.service.TrainingService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
@Api(tags = "Trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @PostMapping
    @ApiOperation("Create trainee profile")
    public ResponseEntity<RegistrationResponse> createTrainee(
            @Valid @RequestBody RegisterTraineeProfileRequest request
    ) {
        return ResponseEntity.ok(traineeService.register(request));
    }

    @RequireAuth
    @GetMapping("/{username}")
    @ApiOperation("Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @PathVariable(value = "username") String username) {
        return ResponseEntity.ok(traineeService.getProfileByUsername(username));
    }

    @RequireAuth
    @PutMapping
    @ApiOperation("Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request
    ) {
        return ResponseEntity.ok(traineeService.update(request));
    }

    @RequireAuth
    @DeleteMapping("/{username}")
    @ApiOperation("Delete trainee profile by username")
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable(value = "username") String username) {
        traineeService.deleteByUsername(username);

        return ResponseEntity.ok().build();
    }

    @RequireAuth
    @GetMapping("/{username}/not-assigned-trainers")
    @ApiOperation("Get active trainers not assigned to trainee")
    public ResponseEntity<List<TrainerShortResponse>> getActiveTrainersNotAssignedToTrainee(
            @PathVariable(value = "username") String username
    ) {
        return ResponseEntity.ok(trainerService.getTrainersNotAssignedToTrainee(username));
    }

    @RequireAuth
    @PutMapping("/trainers")
    @ApiOperation("Update trainee trainers list")
    public ResponseEntity<List<TrainerShortResponse>> updateTraineeTrainersList(
            @Valid @RequestBody UpdateTraineeTrainersRequest request
) {
        return ResponseEntity.ok(traineeService.updateTrainersList(request));
    }

    @RequireAuth
    @GetMapping("/{username}/trainings")
    @ApiOperation("Get trainee trainings list by criteria")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @PathVariable(value = "username") String username,
            @RequestParam(required = false, value = "periodFrom") LocalDate periodFrom,
            @RequestParam(required = false, value = "periodTo") LocalDate periodTo,
            @RequestParam(required = false, value = "trainerName") String trainerName,
            @RequestParam(required = false, value = "trainingTypeId") Long trainingTypeId
    ) {
        return ResponseEntity.ok(trainingService.getTraineeTrainings(
                username,
                periodFrom,
                periodTo,
                trainerName,
                trainingTypeId
        ));
    }

    @RequireAuth
    @PatchMapping("/status")
    @ApiOperation("Activate or de-activate trainee profile")
    public ResponseEntity<Void> changeTraineeStatus(@Valid @RequestBody ChangeActiveStatusRequest request) {
        traineeService.changeActiveStatus(request);

        return ResponseEntity.ok().build();
    }
}
