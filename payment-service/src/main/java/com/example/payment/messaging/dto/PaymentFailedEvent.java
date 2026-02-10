package com.example.payment.messaging.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentFailedEvent extends BaseEvent {

    private Long orderId;
    private String customerId;
    private BigDecimal amount;
    private String reason;
    private String errorCode;

    public PaymentFailedEvent(Long orderId, String customerId, BigDecimal amount,
            String reason, String errorCode) {
        super("PAYMENT_FAILED", "payment-service");
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.reason = reason;
        this.errorCode = errorCode;
    }
}
