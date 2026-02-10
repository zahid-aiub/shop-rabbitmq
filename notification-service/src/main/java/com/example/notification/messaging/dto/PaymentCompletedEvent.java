package com.example.notification.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentCompletedEvent extends BaseEvent {

    private Long orderId;
    private String customerId;
    private Long paymentId;
    private String transactionId;
    private BigDecimal amount;
    private String paymentMethod;
}
