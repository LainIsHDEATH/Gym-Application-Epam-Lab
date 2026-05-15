package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.authentication.AuthService;
import ua.ivan.epam.gym.application.authentication.BasicAuthCredentials;
import ua.ivan.epam.gym.application.authentication.BasicAuthParser;
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
    public ResponseEntity<Void> login(@RequestHeader("Authorization") String authorizationHeader) {
        BasicAuthCredentials credentials = basicAuthParser.parse(authorizationHeader);

        authService.authenticate(credentials.username(), credentials.password());

        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @ApiOperation("Change user password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);

        return ResponseEntity.ok().build();
    }
}
