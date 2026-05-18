package ua.ivan.epam.gym.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.ivan.epam.gym.application.dto.request.AddTrainingRequest;
import ua.ivan.epam.gym.application.model.Training;
import ua.ivan.epam.gym.application.service.TrainingService;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(trainingController)
                .setConversionService(new DefaultFormattingConversionService())
                .build();
    }

    @Test
    void addTrainingShouldReturnOkWhenRequestIsValid() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "John.Smith",
                "Mike.Brown",
                "Morning Cardio",
                LocalDate.of(2026, 5, 5),
                60
        );

        Training training = Training.builder()
                .id(1L)
                .trainingName("Morning Cardio")
                .trainingDate(LocalDate.of(2026, 5, 5))
                .trainingDuration(60)
                .build();

        when(trainingService.create(request))
                .thenReturn(training);

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainingService).create(request);
    }

    @Test
    void addTrainingShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }

    @Test
    void addTrainingShouldReturnBadRequestWhenRequiredFieldsAreInvalid() throws Exception {
        String invalidJson = """
                {
                  "traineeUsername": "",
                  "trainerUsername": "",
                  "trainingName": "",
                  "trainingDate": null,
                  "trainingDuration": 0
                }
                """;

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(trainingService);
    }
}