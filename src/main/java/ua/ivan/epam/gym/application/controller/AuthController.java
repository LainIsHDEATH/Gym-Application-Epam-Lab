package ua.ivan.epam.gym.application.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.ivan.epam.gym.application.authentication.AuthService;
import ua.ivan.epam.gym.application.dto.request.ChangePasswordRequest;
import ua.ivan.epam.gym.application.facade.GymFacade;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Api(tags = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final GymFacade gymFacade;

    @GetMapping("/login")
    @ApiOperation("Login by username and password")
    public ResponseEntity<Void> login(@RequestParam String username,
                                      @RequestParam String password) {
        authService.authenticate(username, password);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/password")
    @ApiOperation("Change user password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        gymFacade.changePassword(request);

        return ResponseEntity.ok().build();
    }
}
