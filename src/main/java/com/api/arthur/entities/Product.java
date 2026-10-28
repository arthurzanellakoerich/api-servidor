package com.api.arthur.entities;

import com.api.arthur.exceptions.BusinessRuleException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "tb_products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private int quantityInStock;

    public int getQuantityInStock() {
        return quantityInStock;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void addStock(int quantity) {
        validateQuantity(quantity);
        this.quantityInStock += quantity;
    }

    public void removeStock(int quantity) {
        validateQuantity(quantity);
        if (quantity > this.quantityInStock) {
            throw new BusinessRuleException("Estoque insuficiente. Disponível: " + quantityInStock);
        }
        this.quantityInStock -= quantity;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessRuleException("A quantidade deve ser maior que zero. ");
        }
    }

}
