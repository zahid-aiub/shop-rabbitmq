package com.example.payment.messaging;

import com.example.payment.messaging.dto.PaymentCompletedEvent;
import com.example.payment.messaging.dto.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchanges.payment}")
    private String paymentExchange;

    @Value("${rabbitmq.routing-keys.payment-completed}")
    private String paymentCompletedRoutingKey;

    @Value("${rabbitmq.routing-keys.payment-failed}")
    private String paymentFailedRoutingKey;

    public EventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent: eventId={}, orderId={}, transactionId={}",
                event.getEventId(), event.getOrderId(), event.getTransactionId());

        rabbitTemplate.convertAndSend(paymentExchange, paymentCompletedRoutingKey, event);

        log.info("Successfully published PaymentCompletedEvent to exchange: {}, routingKey: {}",
                paymentExchange, paymentCompletedRoutingKey);
    }

    public void publishPaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Publishing PaymentFailedEvent: eventId={}, orderId={}, reason={}",
                event.getEventId(), event.getOrderId(), event.getReason());

        rabbitTemplate.convertAndSend(paymentExchange, paymentFailedRoutingKey, event);

        log.info("Successfully published PaymentFailedEvent to exchange: {}, routingKey: {}",
                paymentExchange, paymentFailedRoutingKey);
    }
}
