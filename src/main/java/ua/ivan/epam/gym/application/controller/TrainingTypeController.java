package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.facade.GymFacade;
import ua.ivan.epam.gym.application.mapper.RestResponseMapper;

import java.util.List;

@RestController
@RequestMapping("/api/v1/training-types")
@RequiredArgsConstructor
@Api(tags = "Training Types")
public class TrainingTypeController {

    private final GymFacade gymFacade;
    private final RestResponseMapper mapper;

    @GetMapping
    @ApiOperation("Get training types")
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes(
            @RequestHeader("X-Username") String authUsername,
            @RequestHeader("X-Password") String authPassword
    ) {
        return ResponseEntity.ok(
                gymFacade.getTrainingTypes(authUsername, authPassword)
                        .stream()
                        .map(mapper::toTrainingTypeResponse)
                        .toList()
        );
    }
}