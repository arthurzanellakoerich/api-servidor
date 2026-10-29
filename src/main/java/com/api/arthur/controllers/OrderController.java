package com.api.arthur.controllers;

import com.api.arthur.dtos.OrderRequest;
import com.api.arthur.dtos.OrderResponse;
import com.api.arthur.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll(Authentication authentication) {
        return ResponseEntity.ok(
                orderService.findAll(authentication.getName(), isAdmin(authentication)));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request,
                                                Authentication authentication) {
        OrderResponse response = orderService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable UUID id,
                                                  Authentication authentication) {
        return ResponseEntity.ok(
                orderService.findById(id, authentication.getName(), isAdmin(authentication)));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> pay(@PathVariable UUID id,
                                             Authentication authentication) {
        return ResponseEntity.ok(
                orderService.pay(id, authentication.getName(), isAdmin(authentication)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable UUID id,
                                                Authentication authentication) {
        return ResponseEntity.ok(
                orderService.cancel(id, authentication.getName(), isAdmin(authentication)));
    }
}