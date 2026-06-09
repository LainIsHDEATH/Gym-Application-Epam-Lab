package ua.ivan.epam.gym.workload.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.workload.dto.request.TrainerWorkloadRequest;
import ua.ivan.epam.gym.workload.dto.response.TrainerMonthlyWorkloadResponse;
import ua.ivan.epam.gym.workload.dto.response.TrainerWorkloadResponse;
import ua.ivan.epam.gym.workload.exception.exceptions.EntityNotFoundException;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;
import ua.ivan.epam.gym.workload.repository.TrainerWorkloadRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

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

        when(trainerWorkloadRepository.getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        trainerWorkloadService.updateTrainerWorkload(request);

        verify(trainerWorkloadRepository).getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class));

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getTrainerUsername().equals("Mike.Brown")
                        && workload.getTrainerFirstName().equals("Mike")
                        && workload.getTrainerLastName().equals("Brown")
                        && workload.getIsActive()
                        && workload.getDuration(2026, 5) == 60
        ));
    }

    @Test
    void updateTrainerWorkloadShouldAddDurationToExistingWorkloadWhenActionIsAdd() {
        TrainerWorkload existingWorkload = TrainerWorkload.builder()
                .trainerUsername("Mike.Brown")
                .trainerFirstName("Mike")
                .trainerLastName("Brown")
                .isActive(true)
                .build();

        existingWorkload.addDuration(2026, 5, 45);

        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Michael",
                "Brown",
                false,
                LocalDate.of(2026, 5, 10),
                60,
                WorkloadActionType.ADD
        );

        when(trainerWorkloadRepository.getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class)))
                .thenReturn(existingWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(105, existingWorkload.getDuration(2026, 5));
        assertEquals("Michael", existingWorkload.getTrainerFirstName());
        assertEquals("Brown", existingWorkload.getTrainerLastName());
        assertFalse(existingWorkload.getIsActive());

        verify(trainerWorkloadRepository).getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class));
        verify(trainerWorkloadRepository).save(existingWorkload);
    }

    @Test
    void updateTrainerWorkloadShouldSubtractDurationWhenActionIsDelete() {
        TrainerWorkload existingWorkload = TrainerWorkload.builder()
                .trainerUsername("Mike.Brown")
                .trainerFirstName("Mike")
                .trainerLastName("Brown")
                .isActive(true)
                .build();

        existingWorkload.addDuration(2026, 5, 105);

        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                45,
                WorkloadActionType.DELETE
        );

        when(trainerWorkloadRepository.getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class)))
                .thenReturn(existingWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(60, existingWorkload.getDuration(2026, 5));

        verify(trainerWorkloadRepository).getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class));
        verify(trainerWorkloadRepository).save(existingWorkload);
    }

    @Test
    void updateTrainerWorkloadShouldNotMakeDurationNegativeWhenDeleteDurationIsGreaterThanCurrent() {
        TrainerWorkload existingWorkload = TrainerWorkload.builder()
                .trainerUsername("Mike.Brown")
                .trainerFirstName("Mike")
                .trainerLastName("Brown")
                .isActive(true)
                .build();

        existingWorkload.addDuration(2026, 5, 30);

        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                60,
                WorkloadActionType.DELETE
        );

        when(trainerWorkloadRepository.getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class)))
                .thenReturn(existingWorkload);

        trainerWorkloadService.updateTrainerWorkload(request);

        assertEquals(0, existingWorkload.getDuration(2026, 5));

        verify(trainerWorkloadRepository).save(existingWorkload);
    }

    @Test
    void updateTrainerWorkloadShouldCreateZeroDurationWorkloadWhenDeleteIsReceivedForNewTrainer() {
        TrainerWorkloadRequest request = createRequest(
                "Mike.Brown",
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                60,
                WorkloadActionType.DELETE
        );

        when(trainerWorkloadRepository.getOrCreate(eq("Mike.Brown"), any(TrainerWorkload.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        trainerWorkloadService.updateTrainerWorkload(request);

        verify(trainerWorkloadRepository).save(argThat(workload ->
                workload.getTrainerUsername().equals("Mike.Brown")
                        && workload.getDuration(2026, 5) == 0
        ));
    }

    @Test
    void getMonthlyWorkloadShouldReturnMonthlyWorkloadWhenTrainerExists() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("Mike.Brown")
                .trainerFirstName("Mike")
                .trainerLastName("Brown")
                .isActive(true)
                .build();

        workload.addDuration(2026, 5, 105);

        when(trainerWorkloadRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(workload));

        TrainerMonthlyWorkloadResponse result =
                trainerWorkloadService.getMonthlyWorkload("Mike.Brown", 2026, 5);

        assertEquals("Mike.Brown", result.trainerUsername());
        assertEquals("Mike", result.trainerFirstName());
        assertEquals("Brown", result.trainerLastName());
        assertTrue(result.isActive());
        assertEquals(2026, result.year());
        assertEquals(5, result.month());
        assertEquals(105, result.trainingSummaryDuration());

        verify(trainerWorkloadRepository).findByUsername("Mike.Brown");
    }

    @Test
    void getMonthlyWorkloadShouldReturnZeroWhenTrainerExistsButMonthHasNoWorkload() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("Mike.Brown")
                .trainerFirstName("Mike")
                .trainerLastName("Brown")
                .isActive(true)
                .build();

        when(trainerWorkloadRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(workload));

        TrainerMonthlyWorkloadResponse result =
                trainerWorkloadService.getMonthlyWorkload("Mike.Brown", 2026, 5);

        assertEquals(0, result.trainingSummaryDuration());

        verify(trainerWorkloadRepository).findByUsername("Mike.Brown");
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
    }

    @Test
    void getTrainerWorkloadShouldReturnFullWorkloadSortedByYearAndMonth() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("Mike.Brown")
                .trainerFirstName("Mike")
                .trainerLastName("Brown")
                .isActive(true)
                .build();

        workload.addDuration(2027, 1, 60);
        workload.addDuration(2026, 6, 90);
        workload.addDuration(2026, 5, 105);

        when(trainerWorkloadRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(workload));

        TrainerWorkloadResponse result =
                trainerWorkloadService.getTrainerWorkload("Mike.Brown");

        assertEquals("Mike.Brown", result.trainerUsername());
        assertEquals("Mike", result.trainerFirstName());
        assertEquals("Brown", result.trainerLastName());
        assertTrue(result.isActive());

        assertEquals(2, result.years().size());

        assertEquals(2026, result.years().get(0).year());
        assertEquals(2, result.years().get(0).months().size());
        assertEquals(5, result.years().get(0).months().get(0).month());
        assertEquals(105, result.years().get(0).months().get(0).trainingSummaryDuration());
        assertEquals(6, result.years().get(0).months().get(1).month());
        assertEquals(90, result.years().get(0).months().get(1).trainingSummaryDuration());

        assertEquals(2027, result.years().get(1).year());
        assertEquals(1, result.years().get(1).months().size());
        assertEquals(1, result.years().get(1).months().get(0).month());
        assertEquals(60, result.years().get(1).months().get(0).trainingSummaryDuration());

        verify(trainerWorkloadRepository).findByUsername("Mike.Brown");
    }

    @Test
    void getTrainerWorkloadShouldReturnEmptyYearsWhenTrainerHasNoWorkload() {
        TrainerWorkload workload = TrainerWorkload.builder()
                .trainerUsername("Mike.Brown")
                .trainerFirstName("Mike")
                .trainerLastName("Brown")
                .isActive(true)
                .build();

        when(trainerWorkloadRepository.findByUsername("Mike.Brown"))
                .thenReturn(Optional.of(workload));

        TrainerWorkloadResponse result =
                trainerWorkloadService.getTrainerWorkload("Mike.Brown");

        assertEquals("Mike.Brown", result.trainerUsername());
        assertTrue(result.years().isEmpty());

        verify(trainerWorkloadRepository).findByUsername("Mike.Brown");
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
}