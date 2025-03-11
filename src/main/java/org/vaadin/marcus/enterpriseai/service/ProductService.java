package org.vaadin.marcus.enterpriseai.service;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import org.vaadin.marcus.enterpriseai.data.Product;
import org.vaadin.marcus.enterpriseai.data.ProductRepository;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Tool(description = "Find all products")
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Tool(description = "Finds a Product given its id")
    public Product findById(
        @ToolParam(description = "The id of the Product to find") Long id
    ) {
        return productRepository.findById(id).orElseThrow();
    }

    @Tool(description = "Save a product")
    public Product save(
        @ToolParam(description = "The updated product") Product product
    ) {
        return productRepository.save(product);
    }
}
