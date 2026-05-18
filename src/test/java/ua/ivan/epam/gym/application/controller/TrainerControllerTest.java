package ua.ivan.epam.gym.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import ua.ivan.epam.gym.application.dto.request.ChangeActiveStatusRequest;
import ua.ivan.epam.gym.application.dto.request.RegisterTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTrainerProfileRequest;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.dto.response.TraineeShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerTrainingResponse;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.service.TrainerService;
import ua.ivan.epam.gym.application.service.TrainingService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainerController trainerController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(trainerController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setConversionService(new DefaultFormattingConversionService())
                .build();
    }

    @Test
    void createTrainerShouldReturnRegistrationResponse() throws Exception {
        RegisterTrainerProfileRequest request = new RegisterTrainerProfileRequest(
                "Mike",
                "Brown",
                1L
        );

        RegistrationResponse response = new RegistrationResponse(
                "Mike.Brown",
                "password12"
        );

        when(trainerService.register(request))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Mike.Brown"))
                .andExpect(jsonPath("$.password").value("password12"));

        verify(trainerService).register(request);
        verifyNoInteractions(trainingService);
    }

    @Test
    void createTrainerShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void getTrainerProfileShouldReturnProfileResponse() throws Exception {
        TrainerProfileResponse response = new TrainerProfileResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                new TrainingTypeResponse(1L, "Cardio"),
                true,
                List.of(createTraineeShortResponse())
        );

        when(trainerService.getProfileByUsername("Mike.Brown"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/trainers/{username}", "Mike.Brown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Mike.Brown"))
                .andExpect(jsonPath("$.firstName").value("Mike"))
                .andExpect(jsonPath("$.lastName").value("Brown"))
                .andExpect(jsonPath("$.specialization.id").value(1))
                .andExpect(jsonPath("$.specialization.trainingType").value("Cardio"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainees[0].username").value("John.Smith"))
                .andExpect(jsonPath("$.trainees[0].firstName").value("John"))
                .andExpect(jsonPath("$.trainees[0].lastName").value("Smith"));

        verify(trainerService).getProfileByUsername("Mike.Brown");
        verifyNoInteractions(trainingService);
    }

    @Test
    void updateTrainerProfileShouldReturnUpdatedProfileResponse() throws Exception {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest(
                "Mike.Brown",
                "Michael",
                "Black",
                false
        );

        TrainerProfileResponse response = new TrainerProfileResponse(
                "Mike.Brown",
                "Michael",
                "Black",
                new TrainingTypeResponse(1L, "Cardio"),
                false,
                List.of()
        );

        when(trainerService.update(request))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("Mike.Brown"))
                .andExpect(jsonPath("$.firstName").value("Michael"))
                .andExpect(jsonPath("$.lastName").value("Black"))
                .andExpect(jsonPath("$.specialization.id").value(1))
                .andExpect(jsonPath("$.specialization.trainingType").value("Cardio"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(trainerService).update(request);
        verifyNoInteractions(trainingService);
    }

    @Test
    void updateTrainerProfileShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void getTrainerTrainingsShouldReturnTrainingsByCriteria() throws Exception {
        TrainerTrainingResponse response = new TrainerTrainingResponse(
                "Morning Cardio",
                LocalDate.of(2026, 5, 5),
                new TrainingTypeResponse(1L, "Cardio"),
                60,
                "John.Smith"
        );

        when(trainingService.getTrainerTrainings(
                "Mike.Brown",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "John"
        )).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", "Mike.Brown")
                        .param("periodFrom", "2026-05-01")
                        .param("periodTo", "2026-05-31")
                        .param("traineeName", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Cardio"))
                .andExpect(jsonPath("$[0].trainingDate").value("2026-05-05"))
                .andExpect(jsonPath("$[0].trainingType.id").value(1))
                .andExpect(jsonPath("$[0].trainingType.trainingType").value("Cardio"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].traineeName").value("John.Smith"));

        verify(trainingService).getTrainerTrainings(
                "Mike.Brown",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "John"
        );
        verifyNoInteractions(trainerService);
    }

    @Test
    void getTrainerTrainingsShouldPassNullOptionalCriteriaWhenParamsAreAbsent() throws Exception {
        when(trainingService.getTrainerTrainings(
                "Mike.Brown",
                null,
                null,
                null
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainers/{username}/trainings", "Mike.Brown"))
                .andExpect(status().isOk());

        verify(trainingService).getTrainerTrainings(
                "Mike.Brown",
                null,
                null,
                null
        );
        verifyNoInteractions(trainerService);
    }

    @Test
    void changeTrainerStatusShouldReturnOk() throws Exception {
        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "Mike.Brown",
                false
        );

        mockMvc.perform(patch("/api/v1/trainers/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainerService).changeActiveStatus(request);
        verifyNoInteractions(trainingService);
    }

    @Test
    void changeTrainerStatusShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(patch("/api/v1/trainers/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    private TraineeShortResponse createTraineeShortResponse() {
        return new TraineeShortResponse(
                "John.Smith",
                "John",
                "Smith"
        );
    }
}