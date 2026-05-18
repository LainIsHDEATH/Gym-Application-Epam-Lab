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
@Api(tags = "Trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @PostMapping
    @ApiOperation(value = "Create trainer profile", response = RegistrationResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully created trainer profile"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 404, message = "Training type was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<RegistrationResponse> createTrainer(
            @Valid @RequestBody RegisterTrainerProfileRequest request
    ) {
        return ResponseEntity.ok(trainerService.register(request));
    }

    @RequireAuth
    @GetMapping("/{username}")
    @ApiOperation(value = "Get trainer profile by username", response = TrainerProfileResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully loaded trainer profile"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "Trainer profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable(value = "username") String username
    ) {
        return ResponseEntity.ok(trainerService.getProfileByUsername(username));
    }

    @RequireAuth
    @PutMapping
    @ApiOperation(value = "Update trainer profile", response = TrainerProfileResponse.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated trainer profile"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 404, message = "Trainer profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @Valid @RequestBody UpdateTrainerProfileRequest request
    ) {
        return ResponseEntity.ok(trainerService.update(request));
    }

    @RequireAuth
    @GetMapping("/{username}/trainings")
    @ApiOperation(value = "Get trainer trainings list by criteria", response = TrainerTrainingResponse.class, responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully loaded trainer trainings list"),
            @ApiResponse(code = 400, message = "Invalid request parameters"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable(value = "username") String username,
            @RequestParam(required = false, value = "periodFrom") LocalDate periodFrom,
            @RequestParam(required = false, value = "periodTo") LocalDate periodTo,
            @RequestParam(required = false, value = "traineeName") String traineeName
    ) {
        return ResponseEntity.ok(trainingService.getTrainerTrainings(
                username,
                periodFrom,
                periodTo,
                traineeName
        ));
    }

    @RequireAuth
    @PatchMapping("/status")
    @ApiOperation(value = "Activate or de-activate trainer profile")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully changed trainer profile status"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 401, message = "You are not authorized to update the resource"),
            @ApiResponse(code = 404, message = "Trainer profile was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<Void> changeTrainerStatus(@Valid @RequestBody ChangeActiveStatusRequest request) {
        trainerService.changeActiveStatus(request);

        return ResponseEntity.ok().build();
    }
}