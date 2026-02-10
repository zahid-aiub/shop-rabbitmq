package com.example.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new order")
public class CreateOrderRequest {

    @Schema(description = "Customer identifier", example = "CUST-001", required = true)
    private String customerId;
    @Schema(description = "List of items to order", required = true)
    private List<OrderItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Order item details")
    public static class OrderItemRequest {
        @Schema(description = "Product identifier", example = "PROD-001", required = true)
        private String productId;
        @Schema(description = "Product name", example = "Laptop", required = true)
        private String productName;
        @Schema(description = "Quantity to order", example = "2", required = true, minimum = "1")
        private Integer quantity;
        @Schema(description = "Price per unit", example = "999.99", required = true)
        private BigDecimal price;
    }
}
