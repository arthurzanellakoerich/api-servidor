package com.api.arthur.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        BigDecimal price,
        int quantityInStock
) {
}
