package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:8080")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // 取得所有產品
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    // 取得單一產品
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(product -> ResponseEntity.ok(convertToDTO(product)))
            .orElse(ResponseEntity.notFound().build());
    }

    // 創建產品
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDTO productDTO) {
        try {
            Product product = convertToEntity(productDTO);
            Product savedProduct = productRepository.save(product);
            return ResponseEntity.ok(convertToDTO(savedProduct));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("創建產品失敗: " + e.getMessage());
        }
    }

    // 更新產品
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return productRepository.findById(id)
            .map(existingProduct -> {
                existingProduct.setName(productDTO.getName());
                existingProduct.setPrice(BigDecimal.valueOf(productDTO.getPrice()));
                existingProduct.setDescription(productDTO.getDescription());
                existingProduct.setImageUrl(productDTO.getImageUrl());
                Product updatedProduct = productRepository.save(existingProduct);
                return ResponseEntity.ok(convertToDTO(updatedProduct));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // 刪除產品
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok().body("產品已刪除");
        }
        return ResponseEntity.notFound().build();
    }

    // 搜尋產品
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);
        List<ProductDTO> productDTOs = products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    // 轉換方法：Entity -> DTO
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }

    // 轉換方法：DTO -> Entity
    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        if (dto.getId() != null) {
            product.setId(dto.getId());
        }
        product.setName(dto.getName());
        product.setPrice(BigDecimal.valueOf(dto.getPrice()));
        product.setDescription(dto.getDescription());
        product.setImageUrl(dto.getImageUrl());
        return product;
    }
}