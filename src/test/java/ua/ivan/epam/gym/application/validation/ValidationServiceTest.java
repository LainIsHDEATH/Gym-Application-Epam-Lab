package ua.ivan.epam.gym.application.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.ivan.epam.gym.application.dto.CreateTraineeRequest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidationServiceTest {

    @Mock
    private Validator validator;

    @Mock
    private ConstraintViolation<CreateTraineeRequest> firstNameViolation;

    @Mock
    private ConstraintViolation<CreateTraineeRequest> lastNameViolation;

    @Mock
    private Path firstNamePath;

    @Mock
    private Path lastNamePath;

    @InjectMocks
    private ValidationService validationService;

    @Test
    void validateShouldNotThrowExceptionWhenObjectIsValid() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                "John",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        when(validator.validate(request)).thenReturn(Set.of());

        assertDoesNotThrow(() -> validationService.validate(request));

        verify(validator).validate(request);
    }

    @Test
    void validateShouldThrowExceptionWhenObjectHasSingleViolation() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                "",
                "Smith",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        when(firstNameViolation.getPropertyPath()).thenReturn(firstNamePath);
        when(firstNamePath.toString()).thenReturn("firstName");
        when(firstNameViolation.getMessage()).thenReturn("must not be blank");

        when(validator.validate(request)).thenReturn(Set.of(firstNameViolation));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validate(request)
        );

        assertEquals("firstName: must not be blank", exception.getMessage());

        verify(validator).validate(request);
    }

    @Test
    void validateShouldThrowExceptionWhenObjectHasMultipleViolations() {
        CreateTraineeRequest request = new CreateTraineeRequest(
                "",
                "",
                LocalDate.of(2000, 5, 10),
                "London"
        );

        when(firstNameViolation.getPropertyPath()).thenReturn(firstNamePath);
        when(firstNamePath.toString()).thenReturn("firstName");
        when(firstNameViolation.getMessage()).thenReturn("must not be blank");

        when(lastNameViolation.getPropertyPath()).thenReturn(lastNamePath);
        when(lastNamePath.toString()).thenReturn("lastName");
        when(lastNameViolation.getMessage()).thenReturn("must not be blank");

        when(validator.validate(request)).thenReturn(Set.of(
                firstNameViolation,
                lastNameViolation
        ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validationService.validate(request)
        );

        String message = exception.getMessage();

        assertTrue(message.contains("firstName: must not be blank"));
        assertTrue(message.contains("lastName: must not be blank"));
        assertTrue(message.contains("; "));

        verify(validator).validate(request);
    }
}