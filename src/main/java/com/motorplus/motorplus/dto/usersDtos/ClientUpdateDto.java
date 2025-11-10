package com.motorplus.motorplus.dto.usersDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientUpdateDto(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 80, message = "El nombre no puede exceder 80 caracteres")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 80, message = "El apellido no puede exceder 80 caracteres")
        String lastName,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Email(message = "Formato de correo electrónico inválido")
        @Size(max = 120, message = "El correo electrónico no puede exceder 120 caracteres")
        String email,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 25, message = "El teléfono no puede exceder 25 caracteres")
        String phone
) { }
