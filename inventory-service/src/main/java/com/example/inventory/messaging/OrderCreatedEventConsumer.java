package com.example.inventory.messaging;

import com.example.inventory.messaging.dto.InventoryFailedEvent;
import com.example.inventory.messaging.dto.InventoryReservedEvent;
import com.example.inventory.messaging.dto.OrderCreatedEvent;
import com.example.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedEventConsumer {

    private final InventoryService inventoryService;
    private final EventProducer eventProducer;

    @RabbitListener(queues = "${rabbitmq.queues.order-created}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent: eventId={}, orderId={}",
                event.getEventId(), event.getOrderId());

        try {
            // Process the order and reserve inventory
            InventoryReservedEvent reservedEvent = inventoryService.processOrderCreatedEvent(event);

            // Publish success event
            eventProducer.publishInventoryReservedEvent(reservedEvent);

        } catch (Exception e) {
            log.error("Failed to process order: {}", event.getOrderId(), e);

            // Extract failed product ID from exception message if available
            String failedProductId = extractProductIdFromError(e.getMessage());

            // Publish failure event
            InventoryFailedEvent failedEvent = inventoryService.createInventoryFailedEvent(
                    event,
                    e.getMessage(),
                    failedProductId);
            eventProducer.publishInventoryFailedEvent(failedEvent);
        }
    }

    private String extractProductIdFromError(String errorMessage) {
        // Extract product ID from error message
        if (errorMessage != null && errorMessage.contains("PROD-")) {
            int start = errorMessage.indexOf("PROD-");
            int end = Math.min(start + 8, errorMessage.length());
            return errorMessage.substring(start, end);
        }
        return "UNKNOWN";
    }
}
