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
import ua.ivan.epam.gym.workload.exception.exceptions.SubtractDurationException;
import ua.ivan.epam.gym.workload.mapper.TrainerWorkloadMapper;
import ua.ivan.epam.gym.workload.model.TrainerWorkload;
import ua.ivan.epam.gym.workload.model.WorkloadActionType;
import ua.ivan.epam.gym.workload.repository.TrainerWorkloadRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadServiceTest {

    private static final String TRAINER_USERNAME = "Mike.Brown";
    private static final String UNKNOWN_USERNAME = "Unknown.Trainer";

    @Mock
    private TrainerWorkloadRepository trainerWorkloadRepository;

    @Mock
    private TrainerWorkloadMapper trainerWorkloadMapper;

    @InjectMocks
    private TrainerWorkloadService trainerWorkloadService;

    @Test
    void updateTrainerWorkloadShouldAddDurationWhenActionIsAdd() {
        TrainerWorkloadRequest request = createRequest(
                TRAINER_USERNAME,
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 5),
                60,
                WorkloadActionType.ADD
        );

        trainerWorkloadService.updateTrainerWorkload(request);

        verify(trainerWorkloadRepository).addDuration(request, 2026, 5);

        verifyNoMoreInteractions(trainerWorkloadRepository);
        verifyNoInteractions(trainerWorkloadMapper);
    }

    @Test
    void updateTrainerWorkloadShouldSubtractDurationWhenActionIsDelete() {
        TrainerWorkloadRequest request = createRequest(
                TRAINER_USERNAME,
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                45,
                WorkloadActionType.DELETE
        );

        trainerWorkloadService.updateTrainerWorkload(request);

        verify(trainerWorkloadRepository).subtractDuration(request, 2026, 5);

        verifyNoMoreInteractions(trainerWorkloadRepository);
        verifyNoInteractions(trainerWorkloadMapper);
    }

    @Test
    void updateTrainerWorkloadShouldPropagateSubtractDurationException() {
        TrainerWorkloadRequest request = createRequest(
                TRAINER_USERNAME,
                "Mike",
                "Brown",
                true,
                LocalDate.of(2026, 5, 10),
                60,
                WorkloadActionType.DELETE
        );

        SubtractDurationException repositoryException =
                new SubtractDurationException(
                        "Accumulated duration is insufficient"
                );

        doThrow(repositoryException)
                .when(trainerWorkloadRepository)
                .subtractDuration(request, 2026, 5);

        SubtractDurationException actualException = assertThrows(
                SubtractDurationException.class,
                () -> trainerWorkloadService.updateTrainerWorkload(
                        request
                )
        );

        assertSame(repositoryException, actualException);

        verify(trainerWorkloadRepository).subtractDuration(request, 2026, 5);

        verifyNoMoreInteractions(trainerWorkloadRepository);
        verifyNoInteractions(trainerWorkloadMapper);
    }

    @Test
    void getMonthlyWorkloadShouldReturnMappedResponseWhenTrainerExists() {
        TrainerWorkload workload = createWorkload(
                TRAINER_USERNAME,
                "Mike",
                "Brown",
                true
        );

        TrainerMonthlyWorkloadResponse expectedResponse =
                new TrainerMonthlyWorkloadResponse(
                        TRAINER_USERNAME,
                        "Mike",
                        "Brown",
                        true,
                        2026,
                        5,
                        105
                );

        when(trainerWorkloadRepository.findByTrainerUsername(TRAINER_USERNAME))
                .thenReturn(Optional.of(workload));

        when(trainerWorkloadMapper.toMonthlyResponse(workload, 2026, 5))
                .thenReturn(expectedResponse);

        TrainerMonthlyWorkloadResponse actualResponse =
                trainerWorkloadService.getMonthlyWorkload(
                        TRAINER_USERNAME,
                        2026,
                        5
                );

        assertSame(expectedResponse, actualResponse);

        verify(trainerWorkloadRepository).findByTrainerUsername(TRAINER_USERNAME);
        verify(trainerWorkloadMapper).toMonthlyResponse(workload, 2026, 5);

        verifyNoMoreInteractions(
                trainerWorkloadRepository,
                trainerWorkloadMapper
        );
    }

    @Test
    void getMonthlyWorkloadShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(
                trainerWorkloadRepository.findByTrainerUsername(
                        UNKNOWN_USERNAME
                )
        ).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerWorkloadService.getMonthlyWorkload(
                        UNKNOWN_USERNAME,
                        2026,
                        5
                )
        );

        assertEquals(
                "Trainer workload not found. username=" + UNKNOWN_USERNAME,
                exception.getMessage()
        );

        verify(trainerWorkloadRepository).findByTrainerUsername(UNKNOWN_USERNAME);

        verifyNoMoreInteractions(trainerWorkloadRepository);
        verifyNoInteractions(trainerWorkloadMapper);
    }

    @Test
    void getTrainerWorkloadShouldReturnMappedResponseWhenTrainerExists() {
        TrainerWorkload workload = createWorkload(
                TRAINER_USERNAME,
                "Mike",
                "Brown",
                true
        );

        TrainerWorkloadResponse expectedResponse =
                new TrainerWorkloadResponse(
                        TRAINER_USERNAME,
                        "Mike",
                        "Brown",
                        true,
                        List.of(
                                new YearSummaryResponse(
                                        2026,
                                        List.of(
                                                new MonthSummaryResponse(
                                                        5,
                                                        105
                                                ),
                                                new MonthSummaryResponse(
                                                        6,
                                                        90
                                                )
                                        )
                                )
                        )
                );

        when(trainerWorkloadRepository.findByTrainerUsername(TRAINER_USERNAME))
                .thenReturn(Optional.of(workload));

        when(trainerWorkloadMapper.toResponse(workload))
                .thenReturn(expectedResponse);

        TrainerWorkloadResponse actualResponse =
                trainerWorkloadService.getTrainerWorkload(
                        TRAINER_USERNAME
                );

        assertSame(expectedResponse, actualResponse);

        verify(trainerWorkloadRepository).findByTrainerUsername(TRAINER_USERNAME);
        verify(trainerWorkloadMapper).toResponse(workload);

        verifyNoMoreInteractions(
                trainerWorkloadRepository,
                trainerWorkloadMapper
        );
    }

    @Test
    void getTrainerWorkloadShouldThrowExceptionWhenTrainerDoesNotExist() {
        when(trainerWorkloadRepository.findByTrainerUsername(UNKNOWN_USERNAME))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> trainerWorkloadService.getTrainerWorkload(
                        UNKNOWN_USERNAME
                )
        );

        assertEquals(
                "Trainer workload not found. username=" + UNKNOWN_USERNAME,
                exception.getMessage()
        );

        verify(trainerWorkloadRepository).findByTrainerUsername(UNKNOWN_USERNAME);

        verifyNoMoreInteractions(trainerWorkloadRepository);
        verifyNoInteractions(trainerWorkloadMapper);
    }

    private TrainerWorkloadRequest createRequest(
            String trainerUsername,
            String trainerFirstName,
            String trainerLastName,
            Boolean isActive,
            LocalDate trainingDate,
            Integer trainingDuration,
            WorkloadActionType actionType
    ) {
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

    private TrainerWorkload createWorkload(
            String trainerUsername,
            String trainerFirstName,
            String trainerLastName,
            Boolean isActive
    ) {
        return TrainerWorkload.builder()
                .trainerUsername(trainerUsername)
                .trainerFirstName(trainerFirstName)
                .trainerLastName(trainerLastName)
                .isActive(isActive)
                .build();
    }
}