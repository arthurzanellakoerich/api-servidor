package com.api.arthur.service;

import com.api.arthur.dto.UserRequest;
import com.api.arthur.dto.UserResponse;
import com.api.arthur.entity.Role;
import com.api.arthur.entity.User;
import com.api.arthur.exception.ResourceNotFoundException;
import com.api.arthur.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return toResponse(getUserOrThrow(id));
    }

    @Transactional
    public UserResponse create(UserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password()); // TODO: aplicar hash BCrypt com o Spring Security
        user.setRole(Role.USER);
        return toResponse(userRepository.save(user));
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}