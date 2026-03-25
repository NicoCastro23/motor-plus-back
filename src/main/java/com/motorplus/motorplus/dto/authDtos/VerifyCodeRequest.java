package com.motorplus.motorplus.dto.authDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyCodeRequest(
        @NotBlank(message = "El email es requerido")
        @Email(message = "El email no es válido")
        String email,

        @NotBlank(message = "El código es requerido")
        String code
) {
}
