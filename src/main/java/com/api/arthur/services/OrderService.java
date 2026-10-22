package com.api.arthur.services;

import com.api.arthur.dtos.OrderItemRequest;
import com.api.arthur.dtos.OrderItemResponse;
import com.api.arthur.dtos.OrderRequest;
import com.api.arthur.dtos.OrderResponse;
import com.api.arthur.entitys.Order;
import com.api.arthur.entitys.OrderItem;
import com.api.arthur.entitys.Product;
import com.api.arthur.entitys.User;
import com.api.arthur.exceptions.BusinessRuleException;
import com.api.arthur.exceptions.ResourceNotFoundException;
import com.api.arthur.repositorys.OrderRepository;
import com.api.arthur.repositorys.ProductRepository;
import com.api.arthur.repositorys.UserRepository;
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
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findByUser(UUID userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        return toResponse(getOrderOrThrow(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.userId()));

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

    @Transactional
    public OrderResponse pay(UUID id) {
        Order order = getOrderOrThrow(id);
        order.pay();
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(UUID id) {
        Order order = getOrderOrThrow(id);
        order.cancel();
        return toResponse(order);
    }

    private Order getOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado: " + id));
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