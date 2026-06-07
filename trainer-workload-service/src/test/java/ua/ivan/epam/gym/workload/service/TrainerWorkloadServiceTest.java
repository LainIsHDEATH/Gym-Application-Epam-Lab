package ua.ivan.epam.gym.workload.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.MonthSummaryResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.YearSummaryResponse;
import ua.ivan.epam.gym.workload.exception.exceptions.EntityNotFoundException;
import ua.ivan.epam.gym.workload.mapper.TrainerWorkloadMapper;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;
import ua.ivan.epam.gym.workload.repository.TrainerWorkloadRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private TrainerWorkloadMapper trainerWorkloadMapper;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void updateTrainerWorkloadShouldCreateWorkloadAndAddDurationWhenActionIsAdd() {
        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 5),
                60,
                WorkloadActionType.ADD
        );

        TrainerWorkload initialWorkload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        when(trainerWorkloadMapper.toEntity(request))
                .thenReturn(initialWorkload);

        when(trainerWorkloadRepository.getOrCreate("Mike.Brown", initialWorkload))
                .thenReturn(initialWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(60, initialWorkload.getDuration(2026, 5));
        assertEquals("Mike", initialWorkload.getTrainerFirstName());
        assertEquals("Brown", initialWorkload.getTrainerLastName());
        assertTrue(initialWorkload.getIsActive());

        verify(trainerWorkloadMapper).toEntity(request);
        verify(trainerWorkloadRepository).getOrCreate("Mike.Brown", initialWorkload);
        verify(trainerWorkloadRepository).save(initialWorkload);
    }

    @Test
    void updateTrainerWorkloadShouldAddDurationToExistingWorkloadWhenActionIsAdd() {
        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Michael",
                "Brown",
                false,
                LocalDate.of(2026, 5, 10),
                60,
                WorkloadActionType.ADD
        );

        TrainerWorkload initialWorkload = createWorkload(
                "Mike.Brown",
                "Michael",
                "Brown",
                false
        );

        TrainerWorkload existingWorkload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        existingWorkload.addDuration(2026, 5, 45);

        when(trainerWorkloadMapper.toEntity(request))
                .thenReturn(initialWorkload);

        when(trainerWorkloadRepository.getOrCreate("Mike.Brown", initialWorkload))
                .thenReturn(existingWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(105, existingWorkload.getDuration(2026, 5));
        assertEquals("Michael", existingWorkload.getTrainerFirstName());
        assertEquals("Brown", existingWorkload.getTrainerLastName());
        assertFalse(existingWorkload.getIsActive());

        verify(trainerWorkloadMapper).toEntity(request);
        verify(trainerWorkloadRepository).getOrCreate("Mike.Brown", initialWorkload);
        verify(trainerWorkloadRepository).save(existingWorkload);
    }

    @Test
    void updateTrainerWorkloadShouldSubtractDurationWhenActionIsDelete() {
        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                45,
                WorkloadActionType.DELETE
        );

        TrainerWorkload initialWorkload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        TrainerWorkload existingWorkload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        existingWorkload.addDuration(2026, 5, 105);

        when(trainerWorkloadMapper.toEntity(request))
                .thenReturn(initialWorkload);

        when(trainerWorkloadRepository.getOrCreate("Mike.Brown", initialWorkload))
                .thenReturn(existingWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(60, existingWorkload.getDuration(2026, 5));

        verify(trainerWorkloadMapper).toEntity(request);
        verify(trainerWorkloadRepository).getOrCreate("Mike.Brown", initialWorkload);
        verify(trainerWorkloadRepository).save(existingWorkload);
    }

    @Test
    void updateTrainerWorkloadShouldNotMakeDurationNegativeWhenDeleteDurationIsGreaterThanCurrent() {
        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                60,
                WorkloadActionType.DELETE
        );

        TrainerWorkload initialWorkload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        TrainerWorkload existingWorkload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        existingWorkload.addDuration(2026, 5, 30);

        when(trainerWorkloadMapper.toEntity(request))
                .thenReturn(initialWorkload);

        when(trainerWorkloadRepository.getOrCreate("Mike.Brown", initialWorkload))
                .thenReturn(existingWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(0, existingWorkload.getDuration(2026, 5));

        verify(trainerWorkloadMapper).toEntity(request);
        verify(trainerWorkloadRepository).getOrCreate("Mike.Brown", initialWorkload);
        verify(trainerWorkloadRepository).save(existingWorkload);
    }

    @Test
    void updateTrainerWorkloadShouldSaveInitialWorkloadWhenDeleteIsReceivedForNewTrainer() {
        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                60,
                WorkloadActionType.DELETE
        );

        TrainerWorkload initialWorkload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        when(trainerWorkloadMapper.toEntity(request))
                .thenReturn(initialWorkload);

        when(trainerWorkloadRepository.getOrCreate("Mike.Brown", initialWorkload))
                .thenReturn(initialWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(0, initialWorkload.getDuration(2026, 5));

        verify(trainerWorkloadMapper).toEntity(request);
        verify(trainerWorkloadRepository).getOrCreate("Mike.Brown", initialWorkload);
        verify(trainerWorkloadRepository).save(initialWorkload);
    }

    @Test
    void getMonthlyWorkloadShouldReturnMappedMonthlyWorkloadWhenTrainerExists() {
        TrainerWorkload workload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

        TrainerMonthlyWorkloadResponse response = new TrainerMonthlyWorkloadResponse(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                2026,
                5,
                105
        );

        when(trainerWorkloadRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(workload));

        when(trainerWorkloadMapper.toMonthlyResponse(workload, 2026, 5))
                .thenReturn(response);

        TrainerMonthlyWorkloadResponse result =
                trainerWorkloadService.getMonthlyWorkload("Mike.Brown", 2026, 5);

        assertSame(response, result);

        verify(trainerWorkloadRepository).findByUsername("Mike.Brown");
        verify(trainerWorkloadMapper).toMonthlyResponse(workload, 2026, 5);
    }

    @Test
    void getMonthlyWorkloadShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerWorkloadRepository.findByUsername("Unknown.Trainer"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerWorkloadService.getMonthlyWorkload("Unknown.Trainer", 2026, 5)
        );

        assertEquals(
                "Trainer workload not found. username=Unknown.Trainer",
                exception.getMessage()
        );

        verify(trainerWorkloadRepository).findByUsername("Unknown.Trainer");
        verifyNoInteractions(trainerWorkloadMapper);
    }

    @Test
    void getTrainerWorkloadShouldReturnMappedFullWorkloadWhenTrainerExists() {
        TrainerWorkload workload = createWorkload(
                "Mike.Brown",
                "Mike",
                "Brown",
                true
        );

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
                        )
                )
        );

        when(trainerWorkloadRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(workload));

        when(trainerWorkloadMapper.toResponse(workload))
                .thenReturn(response);

        TrainerWorkloadResponse result =
                trainerWorkloadService.getTrainerWorkload("Mike.Brown");

        assertSame(response, result);

        verify(trainerWorkloadRepository).findByUsername("Mike.Brown");
        verify(trainerWorkloadMapper).toResponse(workload);
    }

    @Test
    void getTrainerWorkloadShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerWorkloadRepository.findByUsername("Unknown.Trainer"))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerWorkloadService.getTrainerWorkload("Unknown.Trainer")
        );

        assertEquals(
                "Trainer workload not found. username=Unknown.Trainer",
                exception.getMessage()
        );

        verify(trainerWorkloadRepository).findByUsername("Unknown.Trainer");
        verifyNoInteractions(trainerWorkloadMapper);
    }

    private TrainerWorkloadRequest createRequest(String trainerUsername,
                                                 String trainerFirstName,
                                                 String trainerLastName,
                                                 Boolean isActive,
                                                 LocalDate trainingDate,
                                                 Integer trainingDuration,
                                                 WorkloadActionType actionType) {
        return new TrainerWorkloadRequest(
                trainerUsername,
                trainerFirstName,
                trainerLastName,
                isActive,
                trainingDate,
                trainingDuration,
                actionType
        );
    }

    private TrainerWorkload createWorkload(String trainerUsername,
                                           String trainerFirstName,
                                           String trainerLastName,
                                           Boolean isActive) {
        return TrainerWorkload.builder()
                .trainerUsername(trainerUsername)
                .trainerFirstName(trainerFirstName)
                .trainerLastName(trainerLastName)
                .isActive(isActive)
                .build();
    }
}