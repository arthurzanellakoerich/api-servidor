package com.api.arthur.entitys;

import com.api.arthur.enums.OrderStatus;
import com.api.arthur.exceptions.BusinessRuleException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {

    }

    public Order(User user) {
        this.user = user;
        this.status = OrderStatus.CREATED;
        this.createdAt = LocalDateTime.now();
    }

    public void addItem(Product product, int quantity) {
        product.removeStock(quantity);
        items.add(new OrderItem(this, product, quantity));
    }

    public void pay() {
        if (status != OrderStatus.CREATED) {
            throw new BusinessRuleException(
                    "Só é possível pagar um pedido com status CREATED. Status atual: " + status);
        }
        this.status = OrderStatus.PAID;
    }

    public void cancel() {
        if (status != OrderStatus.CREATED) {
            throw new BusinessRuleException(
                    "Só é possível cancelar um pedido com status CREATED. Status atual: " + status);
        }
        this.status = OrderStatus.CANCELED;
        items.forEach(item -> item.getProduct().addStock(item.getQuantity()));
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return items; }
}