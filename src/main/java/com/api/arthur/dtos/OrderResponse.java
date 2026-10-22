package com.api.arthur.dtos;

import com.api.arthur.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items,
        BigDecimal total
) {
}