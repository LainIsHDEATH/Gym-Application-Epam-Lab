package ua.ivan.epam.gym.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @PostMapping
    @Operation(summary = "Create trainee profile")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully created trainee profile",
                    content = @Content(schema = @Schema(implementation = RegistrationResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<RegistrationResponse> createTrainee(
            @Valid @RequestBody RegisterTraineeProfileRequest request
    ) {
        return ResponseEntity.ok(traineeService.register(request));
    }

    @RequireAuth
    @GetMapping("/{username}")
    @Operation(summary = "Get trainee profile by username")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully loaded trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "404", description = "Trainee profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @PathVariable String username
    ) {
        return ResponseEntity.ok(traineeService.getProfileByUsername(username));
    }

    @RequireAuth
    @PutMapping
    @Operation(summary = "Update trainee profile")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated trainee profile",
                    content = @Content(schema = @Schema(implementation = TraineeProfileResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to update the resource"),
            @ApiResponse(responseCode = "404", description = "Trainee profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request
    ) {
        return ResponseEntity.ok(traineeService.update(request));
    }

    @RequireAuth
    @DeleteMapping("/{username}")
    @Operation(summary = "Delete trainee profile by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted trainee profile"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to delete the resource"),
            @ApiResponse(responseCode = "404", description = "Trainee profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> deleteTraineeProfile(@PathVariable String username) {
        traineeService.deleteByUsername(username);

        return ResponseEntity.ok().build();
    }

    @RequireAuth
    @GetMapping("/{username}/not-assigned-trainers")
    @Operation(summary = "Get active trainers not assigned to trainee")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully loaded active trainers not assigned to trainee",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerShortResponse.class)))
            ),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "404", description = "Trainee profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<List<TrainerShortResponse>> getActiveTrainersNotAssignedToTrainee(
            @PathVariable String username
    ) {
        return ResponseEntity.ok(trainerService.getTrainersNotAssignedToTrainee(username));
    }

    @RequireAuth
    @PutMapping("/trainers")
    @Operation(summary = "Update trainee trainers list")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated trainee trainers list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerShortResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to update the resource"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<List<TrainerShortResponse>> updateTraineeTrainersList(
            @Valid @RequestBody UpdateTraineeTrainersRequest request
    ) {
        return ResponseEntity.ok(traineeService.updateTrainersList(request));
    }

    @RequireAuth
    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainee trainings list by criteria")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully loaded trainee trainings list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TraineeTrainingResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) Long trainingTypeId
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
    @Operation(summary = "Activate or de-activate trainee profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed trainee profile status"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to update the resource"),
            @ApiResponse(responseCode = "404", description = "Trainee profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> changeTraineeStatus(@Valid @RequestBody ChangeActiveStatusRequest request) {
        traineeService.changeActiveStatus(request);

        return ResponseEntity.ok().build();
    }
}