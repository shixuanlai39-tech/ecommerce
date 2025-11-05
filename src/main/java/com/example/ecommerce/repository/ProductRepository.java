package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 正確命名：findBy + 欄位名 + Containing + IgnoreCase
    List<Product> findByNameContainingIgnoreCase(String keyword);
}