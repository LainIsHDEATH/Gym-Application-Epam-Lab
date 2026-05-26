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
import ua.ivan.epam.gym.application.dto.request.RegisterTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeProfileRequest;
import ua.ivan.epam.gym.application.dto.request.UpdateTraineeTrainersRequest;
import ua.ivan.epam.gym.application.dto.response.RegistrationResponse;
import ua.ivan.epam.gym.application.dto.response.TraineeProfileResponse;
import ua.ivan.epam.gym.application.dto.response.TraineeTrainingResponse;
import ua.ivan.epam.gym.application.dto.response.TrainerShortResponse;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.service.TraineeService;
import ua.ivan.epam.gym.application.service.TrainerService;
import ua.ivan.epam.gym.application.service.TrainingService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TraineeController traineeController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(traineeController)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setConversionService(new DefaultFormattingConversionService())
                .build();
    }

    @Test
    void createTraineeShouldReturnRegistrationResponse() throws Exception {
        RegisterTraineeProfileRequest request = new RegisterTraineeProfileRequest(
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        RegistrationResponse response = new RegistrationResponse(
                "John.Smith",
                "password12"
        );

        when(traineeService.register(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.password").value("password12"));

        verify(traineeService).register(request);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void createTraineeShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void getTraineeProfileShouldReturnProfileResponse() throws Exception {
        TraineeProfileResponse response = new TraineeProfileResponse(
                "John.Smith",
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London",
                true,
                List.of(createTrainerShortResponse())
        );

        when(traineeService.getProfileByUsername("John.Smith"))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/{username}", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.dateOfBirth").value("2000-05-10"))
                .andExpect(jsonPath("$.address").value("London"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andExpect(jsonPath("$.trainers[0].username").value("Mike.Brown"));

        verify(traineeService).getProfileByUsername("John.Smith");
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void updateTraineeProfileShouldReturnUpdatedProfileResponse() throws Exception {
        UpdateTraineeProfileRequest request = new UpdateTraineeProfileRequest(
                "John.Smith",
                "Johnny",
                "Smithson",
                LocalDate.of(2001, 1, 15),
                "Berlin",
                false
        );

        TraineeProfileResponse response = new TraineeProfileResponse(
                "John.Smith",
                "Johnny",
                "Smithson",
                LocalDate.of(2001, 1, 15),
                "Berlin",
                false,
                List.of()
        );

        when(traineeService.update(request)).thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("John.Smith"))
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.lastName").value("Smithson"))
                .andExpect(jsonPath("$.dateOfBirth").value("2001-01-15"))
                .andExpect(jsonPath("$.address").value("Berlin"))
                .andExpect(jsonPath("$.isActive").value(false));

        verify(traineeService).update(request);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void updateTraineeProfileShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void deleteTraineeProfileShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees/{username}", "John.Smith"))
                .andExpect(status().isOk());

        verify(traineeService).deleteByUsername("John.Smith");
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void getActiveTrainersNotAssignedToTraineeShouldReturnTrainers() throws Exception {
        TrainerShortResponse trainer = createTrainerShortResponse();

        when(trainerService.getTrainersNotAssignedToTrainee("John.Smith"))
                .thenReturn(List.of(trainer));

        mockMvc.perform(get("/api/v1/trainees/{username}/not-assigned-trainers", "John.Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Mike.Brown"))
                .andExpect(jsonPath("$[0].firstName").value("Mike"))
                .andExpect(jsonPath("$[0].lastName").value("Brown"))
                .andExpect(jsonPath("$[0].specialization.id").value(1))
                .andExpect(jsonPath("$[0].specialization.trainingType").value("Cardio"));

        verify(trainerService).getTrainersNotAssignedToTrainee("John.Smith");
        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void updateTraineeTrainersListShouldReturnUpdatedTrainers() throws Exception {
        UpdateTraineeTrainersRequest request = new UpdateTraineeTrainersRequest(
                "John.Smith",
                List.of("Mike.Brown")
        );

        TrainerShortResponse trainer = createTrainerShortResponse();

        when(traineeService.updateTrainersList(request))
                .thenReturn(List.of(trainer));

        mockMvc.perform(put("/api/v1/trainees/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("Mike.Brown"))
                .andExpect(jsonPath("$[0].firstName").value("Mike"))
                .andExpect(jsonPath("$[0].lastName").value("Brown"));

        verify(traineeService).updateTrainersList(request);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void updateTraineeTrainersListShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/trainees/trainers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void getTraineeTrainingsShouldReturnTrainingsByCriteria() throws Exception {
        TraineeTrainingResponse response = new TraineeTrainingResponse(
                "Morning Cardio",
                LocalDate.of(2026, 5, 5),
                new TrainingTypeResponse(1L, "Cardio"),
                60,
                "Mike.Brown"
        );

        when(trainingService.getTraineeTrainings(
                "John.Smith",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Mike",
                1L
        )).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/trainees/{username}/trainings", "John.Smith")
                        .param("periodFrom", "2026-05-01")
                        .param("periodTo", "2026-05-31")
                        .param("trainerName", "Mike")
                        .param("trainingTypeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].trainingName").value("Morning Cardio"))
                .andExpect(jsonPath("$[0].trainingDate").value("2026-05-05"))
                .andExpect(jsonPath("$[0].trainingType.id").value(1))
                .andExpect(jsonPath("$[0].trainingType.trainingType").value("Cardio"))
                .andExpect(jsonPath("$[0].trainingDuration").value(60))
                .andExpect(jsonPath("$[0].trainerName").value("Mike.Brown"));

        verify(trainingService).getTraineeTrainings(
                "John.Smith",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31),
                "Mike",
                1L
        );
        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
    }

    @Test
    void getTraineeTrainingsShouldPassNullOptionalCriteriaWhenParamsAreAbsent() throws Exception {
        when(trainingService.getTraineeTrainings(
                "John.Smith",
                null,
                null,
                null,
                null
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/trainees/{username}/trainings", "John.Smith"))
                .andExpect(status().isOk());

        verify(trainingService).getTraineeTrainings(
                "John.Smith",
                null,
                null,
                null,
                null
        );
        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
    }

    @Test
    void changeTraineeStatusShouldReturnOk() throws Exception {
        ChangeActiveStatusRequest request = new ChangeActiveStatusRequest(
                "John.Smith",
                false
        );

        mockMvc.perform(patch("/api/v1/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).changeActiveStatus(request);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    @Test
    void changeTraineeStatusShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(patch("/api/v1/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(traineeService);
        verifyNoInteractions(trainerService);
        verifyNoInteractions(trainingService);
    }

    private TrainerShortResponse createTrainerShortResponse() {
        return new TrainerShortResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                new TrainingTypeResponse(1L, "Cardio")
        );
    }
}