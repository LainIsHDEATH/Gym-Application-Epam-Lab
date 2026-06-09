package ua.ivan.epam.gym.workload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.MonthSummaryResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.YearSummaryResponse;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;
import ua.ivan.epam.gym.workload.service.TrainerWorkloadService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadControllerTest {

    @Mock
    private TrainerWorkloadService trainerWorkloadService;

    @InjectMocks
    private TrainerWorkloadController trainerWorkloadController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(trainerWorkloadController)
                .build();
    }

    @Test
    void updateTrainerWorkloadShouldReturnOkWhenRequestIsValid() throws Exception {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 5),
                60,
                WorkloadActionType.ADD
        );

        mockMvc.perform(post("/api/v1/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerWorkloadService).updateTrainerWorkload(request);
    }

    @Test
    void updateTrainerWorkloadShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void updateTrainerWorkloadShouldReturnBadRequestWhenRequiredFieldsAreInvalid() throws Exception {
        String invalidJson = """
                {
                  "trainerUsername": "",
                  "trainerFirstName": "",
                  "trainerLastName": "",
                  "isActive": null,
                  "trainingDate": null,
                  "trainingDuration": 0,
                  "actionType": null
                }
                """;

        mockMvc.perform(post("/api/v1/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void updateTrainerWorkloadShouldReturnBadRequestWhenActionTypeIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "trainerUsername": "Mike.Brown",
                  "trainerFirstName": "Mike",
                  "trainerLastName": "Brown",
                  "isActive": true,
                  "trainingDate": "2026-05-05",
                  "trainingDuration": 60,
                  "actionType": "INVALID"
                }
                """;

        mockMvc.perform(post("/api/v1/trainer-workloads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void getMonthlyWorkloadShouldReturnMonthlyWorkload() throws Exception {
        TrainerMonthlyWorkloadResponse response = new TrainerMonthlyWorkloadResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                2026,
                5,
                105
        );

        when(trainerWorkloadService.getMonthlyWorkload("Mike.Brown", 2026, 5))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/Mike.Brown/workloads/monthly")
                        .param("year", "2026")
                        .param("month", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("Mike.Brown"))
                .andExpect(jsonPath("$.trainerFirstName").value("Mike"))
                .andExpect(jsonPath("$.trainerLastName").value("Brown"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(5))
                .andExpect(jsonPath("$.trainingSummaryDuration").value(105));

        verify(trainerWorkloadService).getMonthlyWorkload("Mike.Brown", 2026, 5);
    }

    @Test
    void getMonthlyWorkloadShouldReturnBadRequestWhenYearIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/Mike.Brown/workloads/monthly")
                        .param("month", "5"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void getMonthlyWorkloadShouldReturnBadRequestWhenMonthIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/Mike.Brown/workloads/monthly")
                        .param("year", "2026"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void getMonthlyWorkloadShouldReturnBadRequestWhenYearHasInvalidType() throws Exception {
        mockMvc.perform(get("/api/v1/trainers/Mike.Brown/workloads/monthly")
                        .param("year", "invalid")
                        .param("month", "5"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerWorkloadService);
    }

    @Test
    void getTrainerWorkloadShouldReturnFullTrainerWorkload() throws Exception {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                List.of(
                        new YearSummaryResponse(
                                2026,
                                List.of(
                                        new MonthSummaryResponse(5, 105),
                                        new MonthSummaryResponse(6, 90)
                                )
                        ),
                        new YearSummaryResponse(
                                2027,
                                List.of(
                                        new MonthSummaryResponse(1, 60)
                                )
                        )
                )
        );

        when(trainerWorkloadService.getTrainerWorkload("Mike.Brown"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/Mike.Brown/workloads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("Mike.Brown"))
                .andExpect(jsonPath("$.trainerFirstName").value("Mike"))
                .andExpect(jsonPath("$.trainerLastName").value("Brown"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.years[0].year").value(2026))
                .andExpect(jsonPath("$.years[0].months[0].month").value(5))
                .andExpect(jsonPath("$.years[0].months[0].trainingSummaryDuration").value(105))
                .andExpect(jsonPath("$.years[0].months[1].month").value(6))
                .andExpect(jsonPath("$.years[0].months[1].trainingSummaryDuration").value(90))
                .andExpect(jsonPath("$.years[1].year").value(2027))
                .andExpect(jsonPath("$.years[1].months[0].month").value(1))
                .andExpect(jsonPath("$.years[1].months[0].trainingSummaryDuration").value(60));

        verify(trainerWorkloadService).getTrainerWorkload("Mike.Brown");
    }

    @Test
    void getTrainerWorkloadShouldReturnTrainerWorkloadWithEmptyYears() throws Exception {
        TrainerWorkloadResponse response = new TrainerWorkloadResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                List.of()
        );

        when(trainerWorkloadService.getTrainerWorkload("Mike.Brown"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/Mike.Brown/workloads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("Mike.Brown"))
                .andExpect(jsonPath("$.years").isArray())
                .andExpect(jsonPath("$.years").isEmpty());

        verify(trainerWorkloadService).getTrainerWorkload("Mike.Brown");
    }
}