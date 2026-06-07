package ua.ivan.epam.gym.workload.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.service.TrainerWorkloadService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Trainer Workloads", description = "Operations for updating and retrieving trainer workload summaries")
public class TrainerWorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;

    @PostMapping("/trainer-workloads")
    @Operation(summary = "Update trainer workload")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer workload was successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Authentication is required"),
            @ApiResponse(responseCode = "403", description = "Access denied. Service role is required"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> updateTrainerWorkload(
            @Valid @RequestBody TrainerWorkloadRequest request
    ) {
        trainerWorkloadService.updateTrainerWorkload(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainers/{username}/workloads/monthly")
    @Operation(summary = "Get trainer monthly workload")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trainer monthly workload was successfully loaded",
                    content = @Content(schema = @Schema(implementation = TrainerMonthlyWorkloadResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication is required"),
            @ApiResponse(responseCode = "403", description = "Access denied. Service role is required"),
            @ApiResponse(responseCode = "404", description = "Trainer workload was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TrainerMonthlyWorkloadResponse> getMonthlyWorkload(
            @Parameter(description = "Trainer username", example = "Mike.Brown")
            @PathVariable String username,

            @Parameter(description = "Workload year", example = "2026")
            @RequestParam Integer year,

            @Parameter(description = "Workload month", example = "5")
            @RequestParam Integer month
    ) {
        return ResponseEntity.ok(
                trainerWorkloadService.getMonthlyWorkload(username, year, month)
        );
    }

    @GetMapping("/trainers/{username}/workloads")
    @Operation(summary = "Get full trainer workload")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trainer workload was successfully loaded",
                    content = @Content(schema = @Schema(implementation = TrainerWorkloadResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Authentication is required"),
            @ApiResponse(responseCode = "403", description = "Access denied. Service role is required"),
            @ApiResponse(responseCode = "404", description = "Trainer workload was not found"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(
            @Parameter(description = "Trainer username", example = "Mike.Brown")
            @PathVariable String username
    ) {
        return ResponseEntity.ok(
                trainerWorkloadService.getTrainerWorkload(username)
        );
    }
}