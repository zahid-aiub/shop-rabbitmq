package com.example.order.messaging;

import com.example.order.messaging.dto.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchanges.order}")
    private String orderExchange;

    @Value("${rabbitmq.routing-keys.order-created}")
    private String orderCreatedRoutingKey;

    public EventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Publishing OrderCreatedEvent: eventId={}, orderId={}",
                event.getEventId(), event.getOrderId());

        rabbitTemplate.convertAndSend(orderExchange, orderCreatedRoutingKey, event);

        log.info("Successfully published OrderCreatedEvent to exchange: {}, routingKey: {}",
                orderExchange, orderCreatedRoutingKey);
    }
}
