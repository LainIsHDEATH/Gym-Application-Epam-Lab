package ua.ivan.epam.gym.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 32) String oldPassword,
        @NotBlank @Size(max = 32) String newPassword
) {
}
