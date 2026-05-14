package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.dto.request.AddTrainingRequest;
import ua.ivan.epam.gym.application.facade.GymFacade;

@RestController
@RequestMapping("/api/v1/trainings")
@RequiredArgsConstructor
@Api(tags = "Trainings")
public class TrainingController {

    private final GymFacade gymFacade;

    @PostMapping
    @ApiOperation("Add training")
    public ResponseEntity<Void> addTraining(
            @Valid @RequestBody AddTrainingRequest request,
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        gymFacade.addTraining(
                request,
                authUsername,
                authPassword
        );

        return ResponseEntity.ok().build();
    }
}
