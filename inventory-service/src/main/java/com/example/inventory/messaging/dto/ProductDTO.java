package com.example.inventory.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String internalId;
    private String name;
    private String description;
    private String brand;
    private String category;
    private BigDecimal price;
    private String currency;
    private Integer stock;
    private String ean;
    private String color;
    private String size;
    private String availability;
}
