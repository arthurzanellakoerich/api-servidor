package com.api.arthur.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotNull(message = "O usuário é obrigatório.")
        UUID userId,

        @NotEmpty(message = "O pedido deve ter pelo menos um item.")
        List<@Valid OrderItemRequest> items
) {
}