package ua.ivan.epam.gym.application.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.service.TrainingTypeService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeController trainingTypeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(trainingTypeController)
                .build();
    }

    @Test
    void getTrainingTypesShouldReturnTrainingTypes() throws Exception {
        List<TrainingTypeResponse> response = List.of(
                new TrainingTypeResponse(1L, "Cardio"),
                new TrainingTypeResponse(2L, "Strength"),
                new TrainingTypeResponse(3L, "Yoga")
        );

        when(trainingTypeService.getAll())
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].trainingType").value("Cardio"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].trainingType").value("Strength"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].trainingType").value("Yoga"));

        verify(trainingTypeService).getAll();
    }

    @Test
    void getTrainingTypesShouldReturnEmptyListWhenNoTrainingTypesExist() throws Exception {
        when(trainingTypeService.getAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(trainingTypeService).getAll();
    }
}