// ProductDTO.java
package com.example.ecommerce.dto;

public class ProductDTO {
    private Long id;
    private String name;
    private Double price;  // 改成 Double（可為 null）
    private String description;
    private String imageUrl;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }  // 回傳 Double
    public void setPrice(Double price) { this.price = price; }  // 接收 Double

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}