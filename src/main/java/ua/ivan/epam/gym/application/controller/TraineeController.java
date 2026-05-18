package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
    @ApiOperation(value = "Create trainee profile", response = RegistrationResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created trainee profile"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<RegistrationResponse> createTrainee(
            @Valid @RequestBody RegisterTraineeProfileRequest request
    ) {
        return ResponseEntity.ok(traineeService.register(request));
    }

    @RequireAuth
    @GetMapping("/{username}")
    @ApiOperation(value = "Get trainee profile by username", response = TraineeProfileResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully loaded trainee profile"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "Trainee profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @PathVariable(value = "username") String username
    ) {
        return ResponseEntity.ok(traineeService.getProfileByUsername(username));
    }

    @RequireAuth
    @PutMapping
    @ApiOperation(value = "Update trainee profile", response = TraineeProfileResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated trainee profile"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 404, message = "Trainee profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request
    ) {
        return ResponseEntity.ok(traineeService.update(request));
    }

    @RequireAuth
    @DeleteMapping("/{username}")
    @ApiOperation(value = "Delete trainee profile by username")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully deleted trainee profile"),
            @ApiResponse(code = 401, message = "You are not authorized to delete the resource"),
            @ApiResponse(code = 404, message = "Trainee profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable(value = "username") String username) {
        traineeService.deleteByUsername(username);

        return ResponseEntity.ok().build();
    }

    @RequireAuth
    @GetMapping("/{username}/not-assigned-trainers")
    @ApiOperation(value = "Get active trainers not assigned to trainee", response = TrainerShortResponse.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully loaded active trainers not assigned to trainee"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "Trainee profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<List<TrainerShortResponse>> getActiveTrainersNotAssignedToTrainee(
            @PathVariable(value = "username") String username
    ) {
        return ResponseEntity.ok(trainerService.getTrainersNotAssignedToTrainee(username));
    }

    @RequireAuth
    @PutMapping("/trainers")
    @ApiOperation(value = "Update trainee trainers list", response = TrainerShortResponse.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated trainee trainers list"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 404, message = "Trainee or trainer was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<List<TrainerShortResponse>> updateTraineeTrainersList(
            @Valid @RequestBody UpdateTraineeTrainersRequest request
    ) {
        return ResponseEntity.ok(traineeService.updateTrainersList(request));
    }

    @RequireAuth
    @GetMapping("/{username}/trainings")
    @ApiOperation(value = "Get trainee trainings list by criteria", response = TraineeTrainingResponse.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully loaded trainee trainings list"),
            @ApiResponse(code = 400, message = "Invalid request parameters"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
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
    @ApiOperation(value = "Activate or de-activate trainee profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully changed trainee profile status"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 404, message = "Trainee profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<Void> changeTraineeStatus(@Valid @RequestBody ChangeActiveStatusRequest request) {
        traineeService.changeActiveStatus(request);

        return ResponseEntity.ok().build();
    }
}