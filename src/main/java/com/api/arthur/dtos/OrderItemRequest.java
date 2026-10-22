package com.api.arthur.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderItemRequest(
        @NotNull(message = "O produto é obrigatório.")
        UUID productId,

        @NotNull(message = "A quantidade é obrigatória.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        Integer quantity
) {
}