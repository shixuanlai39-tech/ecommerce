package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ProductDTO;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:8080")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // === 取得所有產品 ===
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> productDTOs = products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    // === 取得單一產品 ===
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(product -> ResponseEntity.ok(convertToDTO(product)))
            .orElse(ResponseEntity.notFound().build());
    }

    // === 創建產品（只用 @RequestParam）===
    @PostMapping(consumes = "multipart/form-data")  // 移除 application/json
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") String priceStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        try {
            // 驗證
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("商品名稱不能空白");
            }
            if (priceStr == null || priceStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("價格不能空白");
            }

            Product product = new Product();
            product.setName(name.trim());
            product.setPrice(new BigDecimal(priceStr.trim()));
            product.setDescription(description != null ? description.trim() : null);

            // 處理圖片
            if (image != null && !image.isEmpty()) {
                String imageUrl = saveImage(image);
                product.setImageUrl(imageUrl);
            }

            Product savedProduct = productRepository.save(product);
            return ResponseEntity.ok(convertToDTO(savedProduct));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("創建產品失敗: " + e.getMessage());
        }
    }

    // === 更新產品（只用 @RequestParam）===
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("price") String priceStr,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        return productRepository.findById(id)
            .map(existingProduct -> {
                try {
                    existingProduct.setName(name.trim());
                    existingProduct.setPrice(new BigDecimal(priceStr.trim()));
                    existingProduct.setDescription(description != null ? description.trim() : null);

                    if (image != null && !image.isEmpty()) {
                        String imageUrl = saveImage(image);
                        existingProduct.setImageUrl(imageUrl);
                    }

                    Product updatedProduct = productRepository.save(existingProduct);
                    return ResponseEntity.ok(convertToDTO(updatedProduct));
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("更新失敗: " + e.getMessage());
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // === 刪除產品 ===
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok().body("產品已刪除");
        }
        return ResponseEntity.notFound().build();
    }

    // === 搜尋產品 ===
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);
        List<ProductDTO> productDTOs = products.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    // === 轉換 Entity -> DTO ===
    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice() != null ? product.getPrice().doubleValue() : null);
        dto.setDescription(product.getDescription());
        dto.setImageUrl(product.getImageUrl());
        return dto;
    }

    // === 圖片儲存 ===
    private String saveImage(MultipartFile image) throws IOException {
        String uploadDir = "upload/images/";
        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(image.getInputStream(), filePath);
        return "/upload/images/" + fileName;
    }
}