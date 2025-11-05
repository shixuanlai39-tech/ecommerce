package com.example.ecommerce.service;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // === 取得購物車（用 userId）===
    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    // === 加入購物車（用 userId）===
    public CartItem addToCart(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return null;

        // 檢查是否已存在
        CartItem existing = cartItemRepository.findByUserAndProduct(user, product);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
            return cartItemRepository.save(existing);
        }

        // 新增
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    // === 更新數量 ===
    public boolean updateQuantity(Long userId, Long productId, int quantity) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;

        CartItem item = cartItemRepository.findByUserAndProduct(user, product);
        if (item == null) return false;

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        return true;
    }

    // === 移除商品 ===
    public boolean removeFromCart(Long userId, Long productId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;

        CartItem item = cartItemRepository.findByUserAndProduct(user, product);
        if (item != null) {
            cartItemRepository.delete(item);
            return true;
        }
        return false;
    }

    // === 清空購物車 ===
    public boolean clearCart(Long userId) {
        if (!userRepository.existsById(userId)) return false;
        cartItemRepository.deleteByUserId(userId);
        return true;
    }
}