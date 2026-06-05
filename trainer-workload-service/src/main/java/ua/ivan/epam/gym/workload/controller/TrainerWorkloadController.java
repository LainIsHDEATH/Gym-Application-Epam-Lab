package ua.ivan.epam.gym.workload.controller;

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
public class TrainerWorkloadController {

    private final TrainerWorkloadService trainerWorkloadService;

    @PostMapping("/trainer-workloads")
    public ResponseEntity<Void> updateTrainerWorkload(
            @Valid @RequestBody TrainerWorkloadRequest request
    ) {
        trainerWorkloadService.updateTrainerWorkload(request);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/trainers/{username}/workloads/monthly")
    public ResponseEntity<TrainerMonthlyWorkloadResponse> getMonthlyWorkload(
            @PathVariable String username,
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        return ResponseEntity.ok(
                trainerWorkloadService.getMonthlyWorkload(username, year, month)
        );
    }

    @GetMapping("/trainers/{username}/workloads")
    public ResponseEntity<TrainerWorkloadResponse> getTrainerWorkload(
            @PathVariable String username
    ) {
        return ResponseEntity.ok(
                trainerWorkloadService.getTrainerWorkload(username)
        );
    }
}