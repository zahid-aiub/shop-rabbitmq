package com.example.inventory.messaging;

import com.example.inventory.messaging.dto.InventoryFailedEvent;
import com.example.inventory.messaging.dto.InventoryReservedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class EventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchanges.inventory}")
    private String inventoryExchange;

    @Value("${rabbitmq.routing-keys.inventory-reserved}")
    private String inventoryReservedRoutingKey;

    @Value("${rabbitmq.routing-keys.inventory-failed}")
    private String inventoryFailedRoutingKey;

    public EventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishInventoryReservedEvent(InventoryReservedEvent event) {
        log.info("Publishing InventoryReservedEvent: eventId={}, orderId={}",
                event.getEventId(), event.getOrderId());

        rabbitTemplate.convertAndSend(inventoryExchange, inventoryReservedRoutingKey, event);

        log.info("Successfully published InventoryReservedEvent to exchange: {}, routingKey: {}",
                inventoryExchange, inventoryReservedRoutingKey);
    }

    public void publishInventoryFailedEvent(InventoryFailedEvent event) {
        log.info("Publishing InventoryFailedEvent: eventId={}, orderId={}, reason={}",
                event.getEventId(), event.getOrderId(), event.getReason());

        rabbitTemplate.convertAndSend(inventoryExchange, inventoryFailedRoutingKey, event);

        log.info("Successfully published InventoryFailedEvent to exchange: {}, routingKey: {}",
                inventoryExchange, inventoryFailedRoutingKey);
    }
}
