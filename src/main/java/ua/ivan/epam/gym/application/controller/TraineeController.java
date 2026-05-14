package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.dto.request.ChangeActiveStatusRequest;
import ua.ivan.epam.gym.application.dto.request.RegisterTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.dto.response.*;
import ua.ivan.epam.gym.application.mapper.RestResponseMapper;
import ua.ivan.epam.gym.application.model.Trainee;
import ua.ivan.epam.gym.application.model.Trainer;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.facade.GymFacade;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trainees")
@RequiredArgsConstructor
@Api(tags = "Trainees")
public class TraineeController {

    private final GymFacade gymFacade;
    private final RestResponseMapper mapper;

    @PostMapping
    @ApiOperation("Create trainee profile")
    public ResponseEntity<RegistrationResponse> createTrainee(
            @Valid @RequestBody RegisterTraineeProfileRequest request
    ) {
        Trainee trainee = gymFacade.createTrainee(request);

        return ResponseEntity.ok(
                mapper.toRegistrationResponse(trainee.getUser())
        );
    }

    @GetMapping("/{username}")
    @ApiOperation("Get trainee profile by username")
    public ResponseEntity<TraineeProfileResponse> getTraineeProfile(
            @PathVariable String username,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        Trainee trainee = gymFacade.getTraineeProfile(
                username,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                mapper.toTraineeProfileResponse(trainee)
        );
    }

    @PutMapping
    @ApiOperation("Update trainee profile")
    public ResponseEntity<TraineeProfileResponse> updateTraineeProfile(
            @Valid @RequestBody UpdateTraineeProfileRequest request,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        Trainee trainee = gymFacade.updateTraineeProfile(
                request,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                mapper.toTraineeProfileResponse(trainee)
        );
    }

    @DeleteMapping("/{username}")
    @ApiOperation("Delete trainee profile by username")
    public ResponseEntity<Void> deleteTraineeProfile(
            @PathVariable String username,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        gymFacade.deleteTraineeByUsername(
                username,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/status")
    @ApiOperation("Activate or de-activate trainee profile")
    public ResponseEntity<Void> changeTraineeStatus(
            @Valid @RequestBody ChangeActiveStatusRequest request,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        gymFacade.changeTraineeActiveStatus(
                request.username(),
                request.isActive(),
                authUsername,
                authPassword
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/not-assigned-trainers")
    @ApiOperation("Get active trainers not assigned to trainee")
    public ResponseEntity<List<TrainerShortResponse>> getActiveTrainersNotAssignedToTrainee(
            @PathVariable String username,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        List<Trainer> trainers = gymFacade.getActiveTrainersNotAssignedToTrainee(
                username,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                trainers.stream()
                        .map(mapper::toTrainerShortResponse)
                        .toList()
        );
    }

    @PutMapping("/trainers")
    @ApiOperation("Update trainee trainers list")
    public ResponseEntity<UpdateTraineeTrainersResponse> updateTraineeTrainersList(
            @Valid @RequestBody UpdateTraineeTrainersRequest request,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        Trainee trainee = gymFacade.updateTraineeTrainersList(
                request,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                mapper.toUpdateTraineeTrainersResponse(trainee)
        );
    }

    @GetMapping("/{username}/trainings")
    @ApiOperation("Get trainee trainings list by criteria")
    public ResponseEntity<List<TraineeTrainingResponse>> getTraineeTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate periodFrom,
            @RequestParam(required = false) LocalDate periodTo,
            @RequestParam(required = false) String trainerName,
            @RequestParam(required = false) Long trainingTypeId,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        List<Training> trainings = gymFacade.getTraineeTrainings(
                username,
                periodFrom,
                periodTo,
                trainerName,
                trainingTypeId,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok(
                trainings.stream()
                        .map(mapper::toTraineeTrainingResponse)
                        .toList()
        );
    }
}
