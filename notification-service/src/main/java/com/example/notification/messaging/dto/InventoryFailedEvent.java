package com.example.notification.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryFailedEvent extends BaseEvent {

    private Long orderId;
    private String customerId;
    private String reason;
    private String failedProductId;
}
