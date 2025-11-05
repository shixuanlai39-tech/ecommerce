package com.example.ecommerce.dto;

import lombok.Data;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private double price;  // 改成 double，與前端一致
    private String description;
    private String imageUrl;
}