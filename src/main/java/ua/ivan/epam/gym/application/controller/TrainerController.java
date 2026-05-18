package ua.ivan.epam.gym.application.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation("Create trainer profile")
    public ResponseEntity<RegistrationResponse> createTrainer(
            @Valid @RequestBody RegisterTrainerProfileRequest request
    ) {
        return ResponseEntity.ok(trainerService.register(request));
    }

    @RequireAuth
    @GetMapping("/{username}")
    @ApiOperation("Get trainer profile by username")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable(value = "username") String username) {
        return ResponseEntity.ok(trainerService.getProfileByUsername(username));
    }

    @RequireAuth
    @PutMapping
    @ApiOperation("Update trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @Valid @RequestBody UpdateTrainerProfileRequest request
    ) {
        return ResponseEntity.ok(trainerService.update(request));
    }

    @RequireAuth
    @GetMapping("/{username}/trainings")
    @ApiOperation("Get trainer trainings list by criteria")
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
    @ApiOperation("Activate or de-activate trainer profile")
    public ResponseEntity<Void> changeTrainerStatus(@Valid @RequestBody ChangeActiveStatusRequest request) {
        trainerService.changeActiveStatus(request);

        return ResponseEntity.ok().build();
    }
}
