package ua.ivan.epam.gym.application.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.response.TrainingTypeResponse;
import ua.ivan.epam.gym.application.mapper.TrainingTypeMapper;
import ua.ivan.epam.gym.application.model.TrainingType;
import ua.ivan.epam.gym.application.repository.TrainingTypeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void getAllShouldReturnMappedTrainingTypes() {
        TrainingType cardio = createTrainingType(1L, "Cardio");
        TrainingType strength = createTrainingType(2L, "Strength");

        TrainingTypeResponse cardioResponse = new TrainingTypeResponse(1L, "Cardio");
        TrainingTypeResponse strengthResponse = new TrainingTypeResponse(2L, "Strength");

        when(trainingTypeRepository.findAll())
                .thenReturn(List.of(cardio, strength));

        when(trainingTypeMapper.toTrainingTypeResponse(cardio))
                .thenReturn(cardioResponse);

        when(trainingTypeMapper.toTrainingTypeResponse(strength))
                .thenReturn(strengthResponse);

        List<TrainingTypeResponse> result = trainingTypeService.getAll();

        assertEquals(2, result.size());
        assertSame(cardioResponse, result.get(0));
        assertSame(strengthResponse, result.get(1));

        verify(trainingTypeRepository).findAll();
        verify(trainingTypeMapper).toTrainingTypeResponse(cardio);
        verify(trainingTypeMapper).toTrainingTypeResponse(strength);
    }

    @Test
    void getAllShouldReturnEmptyListWhenNoTrainingTypesExist() {
        when(trainingTypeRepository.findAll())
                .thenReturn(List.of());

        List<TrainingTypeResponse> result = trainingTypeService.getAll();

        assertTrue(result.isEmpty());

        verify(trainingTypeRepository).findAll();
        verifyNoInteractions(trainingTypeMapper);
    }

    private TrainingType createTrainingType(Long id, String name) {
        return TrainingType.builder()
                .id(id)
                .trainingTypeName(name)
                .build();
    }
}