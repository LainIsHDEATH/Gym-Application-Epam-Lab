package ua.ivan.epam.gym.application.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.authentication.AuthService;
import ua.ivan.epam.gym.application.authentication.BasicAuthCredentials;
import ua.ivan.epam.gym.application.authentication.BasicAuthParser;
import ua.ivan.epam.gym.application.authentication.RequireAuth;
import ua.ivan.epam.gym.application.dto.request.ChangePasswordRequest;
import ua.ivan.epam.gym.application.service.UserService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final BasicAuthParser basicAuthParser;

    @GetMapping("/login")
    @Operation(summary = "Authenticate user with Basic Authorization header")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> login(@RequestHeader("Authorization") String authorizationHeader) {
        BasicAuthCredentials credentials = basicAuthParser.parse(authorizationHeader);

        authService.authenticate(credentials.username(), credentials.password());

        return ResponseEntity.ok().build();
    }

    @RequireAuth
    @PutMapping("/password")
    @Operation(summary = "Change user password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully changed user password"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password"),
            @ApiResponse(responseCode = "500", description = "Application failed to process the request")
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);

        return ResponseEntity.ok().build();
    }
}