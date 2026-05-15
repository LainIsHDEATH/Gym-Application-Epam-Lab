package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation("Add training")
    public ResponseEntity<Void> addTraining(@Valid @RequestBody AddTrainingRequest request) {
        trainingService.create(request);

        return ResponseEntity.ok().build();
    }
}
