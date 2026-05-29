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
import ua.ivan.epam.gym.application.dto.request.ChangePasswordRequest;
import ua.ivan.epam.gym.application.dto.request.LoginRequest;
import ua.ivan.epam.gym.application.dto.response.LoginResponse;
import ua.ivan.epam.gym.application.service.AuthService;
import ua.ivan.epam.gym.application.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

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
    void loginShouldReturnOkAndTokenWhenRequestIsValid() throws Exception {
        LoginRequest request = new LoginRequest(
                "John.Smith",
                "password12"
        );

        LoginResponse response = new LoginResponse(
                "jwt-token",
                "Bearer",
                3600L
        );

        when(authService.login(request))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(3600));

        verify(authService).login(request);
        verifyNoInteractions(userService);
    }

    @Test
    void loginShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
        verifyNoInteractions(userService);
    }

    @Test
    void loginShouldReturnBadRequestWhenUsernameIsBlank() throws Exception {
        LoginRequest request = new LoginRequest(
                "",
                "password12"
        );

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
        verifyNoInteractions(userService);
    }

    @Test
    void loginShouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
        LoginRequest request = new LoginRequest(
                "John.Smith",
                ""
        );

        mockMvc.perform(post("/api/v1/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
        verifyNoInteractions(userService);
    }

    @Test
    void logoutShouldReturnOkWhenAuthorizationHeaderIsValid() throws Exception {
        String authorizationHeader = "Bearer jwt-token";

        mockMvc.perform(post("/api/v1/logout")
                        .header("Authorization", authorizationHeader))
                .andExpect(status().isOk());

        verify(authService).logout(authorizationHeader);
        verifyNoInteractions(userService);
    }

    @Test
    void logoutShouldReturnBadRequestWhenAuthorizationHeaderIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/logout"))
                .andExpect(status().isBadRequest());

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
    }

    @Test
    void changePasswordShouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
        verifyNoInteractions(authService);
    }
}