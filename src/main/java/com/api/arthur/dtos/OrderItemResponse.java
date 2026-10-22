package com.api.arthur.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}