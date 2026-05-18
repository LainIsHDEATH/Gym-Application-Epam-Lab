package ua.ivan.epam.gym.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ua.ivan.epam.gym.application.authentication.AuthService;
import ua.ivan.epam.gym.application.authentication.BasicAuthCredentials;
import ua.ivan.epam.gym.application.authentication.BasicAuthParser;
import ua.ivan.epam.gym.application.dto.request.ChangePasswordRequest;
import ua.ivan.epam.gym.application.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private BasicAuthParser basicAuthParser;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(authController)
                .build();
    }

    @Test
    void loginShouldReturnOkWhenCredentialsAreValid() throws Exception {
        String authorizationHeader = "Basic Sm9obi5TbWl0aDpwYXNzd29yZDEy";

        BasicAuthCredentials credentials = new BasicAuthCredentials(
                "John.Smith",
                "password12"
        );

        when(basicAuthParser.parse(authorizationHeader))
                .thenReturn(credentials);

        mockMvc.perform(get("/api/v1/login")
                        .header("Authorization", authorizationHeader))
                .andExpect(status().isOk());

        verify(basicAuthParser).parse(authorizationHeader);
        verify(authService).authenticate("John.Smith", "password12");
        verifyNoInteractions(userService);
    }

    @Test
    void loginShouldReturnBadRequestWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/login"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(basicAuthParser);
        verifyNoInteractions(authService);
        verifyNoInteractions(userService);
    }

    @Test
    void changePasswordShouldReturnOkWhenRequestIsValid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "John.Smith",
                "oldPassword",
                "newPassword"
        );

        mockMvc.perform(put("/api/v1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userService).changePassword(request);
        verifyNoInteractions(authService);
        verifyNoInteractions(basicAuthParser);
    }

    @Test
    void changePasswordShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(authService);
        verifyNoInteractions(basicAuthParser);
    }
}