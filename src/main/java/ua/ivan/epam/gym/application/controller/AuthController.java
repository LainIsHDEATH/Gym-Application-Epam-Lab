package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final BasicAuthParser basicAuthParser;

    @GetMapping("/login")
    @ApiOperation(value = "Authenticate user with Basic Authorization header")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully authenticated"),
            @ApiResponse(code = 400, message = "Missing or invalid Authorization header"),
            @ApiResponse(code = 401, message = "Invalid username or password"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<Void> login(@RequestHeader("Authorization") String authorizationHeader) {
        BasicAuthCredentials credentials = basicAuthParser.parse(authorizationHeader);

        authService.authenticate(credentials.username(), credentials.password());

        return ResponseEntity.ok().build();
    }

    @RequireAuth
    @PutMapping("/password")
    @ApiOperation(value = "Change user password")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully changed user password"),
            @ApiResponse(code = 400, message = "Invalid request body or validation error"),
            @ApiResponse(code = 401, message = "Invalid username or password"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);

        return ResponseEntity.ok().build();
    }
}