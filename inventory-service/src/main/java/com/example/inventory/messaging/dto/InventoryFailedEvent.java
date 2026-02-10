package com.example.inventory.messaging.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryFailedEvent extends BaseEvent {

    private Long orderId;
    private String customerId;
    private String reason;
    private String failedProductId;

    public InventoryFailedEvent(Long orderId, String customerId,
            String reason, String failedProductId) {
        super("INVENTORY_FAILED", "inventory-service");
        this.orderId = orderId;
        this.customerId = customerId;
        this.reason = reason;
        this.failedProductId = failedProductId;
    }
}
