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
import ua.ivan.epam.gym.application.dto.request.AddTrainingRequest;
import ua.ivan.epam.gym.application.service.TrainingService;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Api(tags = "Trainings")
public class TrainingController {

    private final TrainingService trainingService;

    @RequireAuth
    @PostMapping
    @ApiOperation(value = "Add training")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully added training"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 401, message = "You are not authorized to create the resource"),
            @ApiResponse(code = 404, message = "Trainee or trainer was not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        trainingService.create(request);

        return ResponseEntity.ok().build();
    }
}