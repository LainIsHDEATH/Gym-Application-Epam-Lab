package ua.ivan.epam.gym.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.authentication.RequireAuth;
import ua.ivan.epam.gym.application.dto.request.AddTrainingRequest;
import ua.ivan.epam.gym.application.service.TrainingService;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Tag(name = "Trainings")
public class TrainingController {

    private final TrainingService trainingService;

    @RequireAuth
    @PostMapping
    @Operation(summary = "Add training")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added training"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to create the resource"),
            @ApiResponse(responseCode = "404", description = "Trainee or trainer was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        trainingService.create(request);

        return ResponseEntity.ok().build();
    }
}