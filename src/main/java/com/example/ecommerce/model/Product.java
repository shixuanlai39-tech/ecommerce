package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;  // 關鍵！加這行

@Data
@NoArgsConstructor
@Entity
@Table(name = "product")
@JsonIgnoreProperties({"cartItems"})  // 關鍵！撕掉「我被誰買了」的便利貼
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image_url")
    private String imageUrl;

    // Lombok 自動產生 getters, setters, toString...
}