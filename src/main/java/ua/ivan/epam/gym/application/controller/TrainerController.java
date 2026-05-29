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
import ua.ivan.epam.gym.application.dto.request.ChangeActiveStatusRequest;
import ua.ivan.epam.gym.application.dto.request.RegisterTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerTrainingResponse;
import ua.ivan.epam.gym.application.service.TrainerService;
import ua.ivan.epam.gym.application.service.TrainingService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @PostMapping
    @Operation(summary = "Create trainer profile")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully created trainer profile",
                    content = @Content(schema = @Schema(implementation = RegistrationResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "404", description = "Training type was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<RegistrationResponse> createTrainer(
            @Valid @RequestBody RegisterTrainerProfileRequest request
    ) {
        return ResponseEntity.ok(trainerService.register(request));
    }

    @GetMapping("/{username}")
    @Operation(summary = "Get trainer profile by username")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully loaded trainer profile",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "404", description = "Trainer profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable String username
    ) {
        return ResponseEntity.ok(trainerService.getProfileByUsername(username));
    }

    @PutMapping
    @Operation(summary = "Update trainer profile")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully updated trainer profile",
                    content = @Content(schema = @Schema(implementation = TrainerProfileResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to update the resource"),
            @ApiResponse(responseCode = "404", description = "Trainer profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @Valid @RequestBody UpdateTrainerProfileRequest request
    ) {
        return ResponseEntity.ok(trainerService.update(request));
    }

    @GetMapping("/{username}/trainings")
    @Operation(summary = "Get trainer trainings list by criteria")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully loaded trainer trainings list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TrainerTrainingResponse.class)))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String traineeName
    ) {
        return ResponseEntity.ok(trainingService.getTrainerTrainings(
                username,
                periodFrom,
                periodTo,
                traineeName
        ));
    }

    @PatchMapping("/status")
    @Operation(summary = "Activate or de-activate trainer profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed trainer profile status"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to update the resource"),
            @ApiResponse(responseCode = "404", description = "Trainer profile was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> changeTrainerStatus(@Valid @RequestBody ChangeActiveStatusRequest request) {
        trainerService.changeActiveStatus(request);

        return ResponseEntity.ok().build();
    }
}