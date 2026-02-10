package com.example.payment.messaging;

import com.example.payment.messaging.dto.InventoryReservedEvent;
import com.example.payment.messaging.dto.PaymentCompletedEvent;
import com.example.payment.messaging.dto.PaymentFailedEvent;
import com.example.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryReservedEventConsumer {

    private final PaymentService paymentService;
    private final EventProducer eventProducer;

    @RabbitListener(queues = "${rabbitmq.queues.inventory-reserved}")
    public void handleInventoryReservedEvent(InventoryReservedEvent event) {
        log.info("Received InventoryReservedEvent: eventId={}, orderId={}",
                event.getEventId(), event.getOrderId());

        try {
            // Process payment
            PaymentCompletedEvent completedEvent = paymentService.processPayment(event);

            // Publish success event
            eventProducer.publishPaymentCompletedEvent(completedEvent);

        } catch (PaymentService.PaymentFailedException e) {
            log.error("Payment failed for order: {}", event.getOrderId(), e);

            // Publish failure event
            PaymentFailedEvent failedEvent = paymentService.createPaymentFailedEvent(
                    event,
                    e.getMessage(),
                    e.getErrorCode());
            eventProducer.publishPaymentFailedEvent(failedEvent);

        } catch (Exception e) {
            log.error("Unexpected error processing payment for order: {}", event.getOrderId(), e);

            // Publish failure event with generic error
            PaymentFailedEvent failedEvent = paymentService.createPaymentFailedEvent(
                    event,
                    "Internal payment processing error",
                    "ERR_UNKNOWN");
            eventProducer.publishPaymentFailedEvent(failedEvent);
        }
    }
}
