package com.api.arthur.dto;

import com.api.arthur.entity.Role;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Role role
) {
}