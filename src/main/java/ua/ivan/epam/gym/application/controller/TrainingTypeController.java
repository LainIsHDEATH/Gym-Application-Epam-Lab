package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.authentication.RequireAuth;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.service.TrainingTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
@RequiredArgsConstructor
@Api(tags = "Training Types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    @RequireAuth
    @GetMapping
    @ApiOperation("Get training types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        return ResponseEntity.ok(trainingTypeService.getAll());
    }
}