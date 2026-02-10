package com.example.inventory.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryReservedEvent extends BaseEvent {

    private Long orderId;
    private String customerId;
    private List<ReservedItemDTO> items;
    private BigDecimal totalAmount;

    public InventoryReservedEvent(Long orderId, String customerId,
            List<ReservedItemDTO> items, BigDecimal totalAmount) {
        super("INVENTORY_RESERVED", "inventory-service");
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservedItemDTO {
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
