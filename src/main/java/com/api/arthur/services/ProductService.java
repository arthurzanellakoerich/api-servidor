package com.api.arthur.services;

import com.api.arthur.dtos.ProductRequest;
import com.api.arthur.dtos.ProductResponse;
import com.api.arthur.dtos.StockRequest;
import com.api.arthur.entities.Product;
import com.api.arthur.exceptions.ResourceNotFoundException;
import com.api.arthur.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        return toResponse(getProductOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setPrice(request.price());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getProductOrThrow(id);
        product.setName(request.name());
        product.setPrice(request.price());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        productRepository.delete(getProductOrThrow(id));
    }

    @Transactional
    public ProductResponse addStock(UUID id, StockRequest request) {
        Product product = getProductOrThrow(id);
        product.addStock(request.quantity());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse removeStock(UUID id, StockRequest request) {
        Product product = getProductOrThrow(id);
        product.removeStock(request.quantity());
        return toResponse(productRepository.save(product));
    }

    private Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getQuantityInStock()
        );
    }
}