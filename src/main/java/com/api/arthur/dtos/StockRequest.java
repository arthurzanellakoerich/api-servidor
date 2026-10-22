package com.api.arthur.dtos;

import jakarta.validation.constraints.Positive;

public record StockRequest(
        @Positive(message = "A quantidade deve ser maior que zero.")
        int quantity
) {
}