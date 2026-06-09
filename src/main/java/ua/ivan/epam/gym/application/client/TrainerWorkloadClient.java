package ua.ivan.epam.gym.application.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ua.ivan.epam.gym.application.config.TrainerWorkloadFeignConfig;
import ua.ivan.epam.gym.application.dto.request.TrainerWorkloadRequest;

@FeignClient(
        name = "trainer-workload-service",
        configuration = TrainerWorkloadFeignConfig.class
)
public interface TrainerWorkloadClient {

    @PostMapping("/api/v1/trainer-workloads")
    void updateTrainerWorkload(@RequestBody TrainerWorkloadRequest request);
}