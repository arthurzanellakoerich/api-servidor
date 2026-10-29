package com.api.arthur.services;

import com.api.arthur.dtos.OrderItemRequest;
import com.api.arthur.dtos.OrderItemResponse;
import com.api.arthur.dtos.OrderRequest;
import com.api.arthur.dtos.OrderResponse;
import com.api.arthur.entities.Order;
import com.api.arthur.entities.OrderItem;
import com.api.arthur.entities.Product;
import com.api.arthur.entities.User;
import com.api.arthur.exceptions.BusinessRuleException;
import com.api.arthur.exceptions.ResourceNotFoundException;
import com.api.arthur.repositories.OrderRepository;
import com.api.arthur.repositories.ProductRepository;
import com.api.arthur.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(String email, boolean isAdmin) {
        List<Order> orders = isAdmin
                ? orderRepository.findAll()
                : orderRepository.findByUserEmail(email);

        return orders.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(UUID userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse create(String email, OrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));

        Order order = new Order(user);
        Set<UUID> productIds = new HashSet<>();

        for (OrderItemRequest itemRequest : request.items()) {
            if (!productIds.add(itemRequest.productId())) {
                throw new BusinessRuleException("Produto repetido no pedido: " + itemRequest.productId());
            }
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Produto não encontrado: " + itemRequest.productId()));
            order.addItem(product, itemRequest.quantity());
        }

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id, String email, boolean isAdmin) {
        return toResponse(getOrderForUser(id, email, isAdmin));
    }

    @Transactional
    public OrderResponse pay(UUID id, String email, boolean isAdmin) {
        Order order = getOrderForUser(id, email, isAdmin);
        order.pay();
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(UUID id, String email, boolean isAdmin) {
        Order order = getOrderForUser(id, email, isAdmin);
        order.cancel();
        return toResponse(order);
    }

    private Order getOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
    }

    private Order getOrderForUser(UUID id, String email, boolean isAdmin) {
        Order order = getOrderOrThrow(id);
        boolean isOwner = order.getUser().getEmail().equals(email);

        if (!isAdmin && !isOwner) {
            throw new ResourceNotFoundException("Pedido não encontrado: " + id);
        }
        return order;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getStatus(),
                order.getCreatedAt(),
                items,
                order.getTotal()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getSubtotal()
        );
    }
}