package ua.ivan.epam.gym.application.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
import ua.ivan.epam.gym.application.facade.GymFacade;
import ua.ivan.epam.gym.application.mapper.RestResponseMapper;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Api(tags = "Trainers")
public class TrainerController {

    private final GymFacade gymFacade;
    private final RestResponseMapper mapper;

    @PostMapping
    @ApiOperation("Create trainer profile")
    public ResponseEntity<RegistrationResponse> createTrainer(
            @Valid @RequestBody RegisterTrainerProfileRequest request
    ) {
        Trainer trainer = gymFacade.createTrainer(request);

        return ResponseEntity.ok(
                mapper.toRegistrationResponse(trainer.getUser())
        );
    }

    @GetMapping("/{username}")
    @ApiOperation("Get trainer profile by username")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(
            @PathVariable String username,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        Trainer trainer = gymFacade.getTrainerProfile(
                username,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                mapper.toTrainerProfileResponse(trainer)
        );
    }

    @PutMapping
    @ApiOperation("Update trainer profile")
    public ResponseEntity<TrainerProfileResponse> updateTrainerProfile(
            @Valid @RequestBody UpdateTrainerProfileRequest request,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        Trainer trainer = gymFacade.updateTrainerProfile(
                request,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                mapper.toTrainerProfileResponse(trainer)
        );
    }

    @PatchMapping("/status")
    @ApiOperation("Activate or de-activate trainer profile")
    public ResponseEntity<Void> changeTrainerStatus(
            @Valid @RequestBody ChangeActiveStatusRequest request,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        gymFacade.changeTrainerActiveStatus(
                request.username(),
                request.isActive(),
                authUsername,
                authPassword
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @ApiOperation("Get trainer trainings list by criteria")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String traineeName,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        List<Training> trainings = gymFacade.getTrainerTrainings(
                username,
                periodFrom,
                periodTo,
                traineeName,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                trainings.stream()
                        .map(mapper::toTrainerTrainingResponse)
                        .toList()
        );
    }
}
