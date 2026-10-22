package com.api.arthur.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres.")
        String password
) {
}