package com.example.payment.messaging.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentCompletedEvent extends BaseEvent {

    private Long orderId;
    private String customerId;
    private Long paymentId;
    private String transactionId;
    private BigDecimal amount;
    private String paymentMethod;

    public PaymentCompletedEvent(Long orderId, String customerId, Long paymentId,
            String transactionId, BigDecimal amount, String paymentMethod) {
        super("PAYMENT_COMPLETED", "payment-service");
        this.orderId = orderId;
        this.customerId = customerId;
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}
