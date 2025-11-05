// src/main/java/com/example/ecommerce/controller/CartController.java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CartItemDTO;
import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:8080")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    // === 加入購物車 ===
    @PostMapping
    @Transactional
    public ResponseEntity<?> addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(401).body("使用者不存在");

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null)
            return ResponseEntity.status(404).body("商品不存在");

        CartItem existing = cartItemRepository.findByUserAndProduct(user, product);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemRepository.save(existing);
        } else {
            CartItem item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "商品已加入購物車"));
    }

    // === 取得購物車 ===
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<List<CartItemDTO>> getCart(@RequestParam Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        
        List<CartItemDTO> dtos = items.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(dtos);
    }

    // === 更新數量 ===
    @PostMapping("/update")
    @Transactional
    public ResponseEntity<?> updateQuantity(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam int quantity) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(401).body("請先登入");

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null)
            return ResponseEntity.status(404).body("商品不存在");

        CartItem item = cartItemRepository.findByUserAndProduct(user, product);
        if (item == null)
            return ResponseEntity.status(404).body("商品不在購物車");

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return ResponseEntity.ok(Map.of("success", true, "message", "商品已從購物車移除"));
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
            return ResponseEntity.ok(Map.of("success", true, "message", "數量已更新"));
        }
    }

    // === 移除商品 ===
    @DeleteMapping
    @Transactional
    public ResponseEntity<?> removeFromCart(
            @RequestParam Long userId,
            @RequestParam Long productId) {

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(401).body("請先登入");

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null)
            return ResponseEntity.status(404).body("商品不存在");

        CartItem item = cartItemRepository.findByUserAndProduct(user, product);
        if (item != null) {
            cartItemRepository.delete(item);
            return ResponseEntity.ok(Map.of("success", true, "message", "商品已從購物車移除"));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "商品不在購物車中"));
    }

    // === 清空購物車 ===
    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return ResponseEntity.status(401).body("請先登入");

        List<CartItem> items = cartItemRepository.findByUserId(userId);
        if (!items.isEmpty()) {
            cartItemRepository.deleteAll(items);
        }
        
        return ResponseEntity.ok(Map.of("success", true, "message", "購物車已清空"));
    }

    // === Entity → DTO 轉換（配合您的 CartItemDTO）===
    private CartItemDTO toDTO(CartItem item) {
        Product product = item.getProduct();
        if (product == null) {
            return new CartItemDTO(); // 防呆
        }

        return new CartItemDTO(
            item.getId(),
            product.getId(),
            product.getName(),
            product.getPrice(),                    // BigDecimal
            product.getImageUrl(),
            product.getDescription(),
            item.getQuantity()
        );
    }
}