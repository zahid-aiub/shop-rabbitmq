package com.example.order.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {

    private Long orderId;
    private String customerId;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;

    public OrderCreatedEvent(Long orderId, String customerId, List<OrderItemDTO> items, BigDecimal totalAmount) {
        super("ORDER_CREATED", "order-service");
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
