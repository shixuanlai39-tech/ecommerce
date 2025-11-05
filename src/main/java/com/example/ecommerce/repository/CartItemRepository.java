package com.example.ecommerce.repository;

import com.example.ecommerce.model.CartItem;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 查詢「某使用者」的「某商品」是否已存在
    CartItem findByUserAndProduct(User user, Product product);

    // 查詢「某使用者」的全部購物車項目（強制載入 product）
    @EntityGraph(attributePaths = {"product"})
    List<CartItem> findByUserId(Long userId);

    // 刪除「某使用者」的全部購物車項目
    void deleteByUserId(Long userId);
}