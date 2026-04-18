package com.motorplus.motorplus.dto.authDtos;

import com.motorplus.motorplus.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es requerida")
        String currentPassword,

        @NotBlank(message = "La nueva contraseña es requerida")
        @ValidPassword
        String newPassword
) {
}

