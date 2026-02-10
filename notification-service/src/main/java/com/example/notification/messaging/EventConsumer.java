package com.example.notification.messaging;

import com.example.notification.messaging.dto.InventoryFailedEvent;
import com.example.notification.messaging.dto.PaymentCompletedEvent;
import com.example.notification.messaging.dto.PaymentFailedEvent;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queues.inventory-failed}")
    public void handleInventoryFailedEvent(InventoryFailedEvent event) {
        log.info("Received InventoryFailedEvent: eventId={}, orderId={}",
                event.getEventId(), event.getOrderId());

        try {
            notificationService.sendInventoryFailedNotification(
                    event.getOrderId(),
                    event.getCustomerId(),
                    event.getReason(),
                    event.getFailedProductId());
        } catch (Exception e) {
            log.error("Failed to send inventory failed notification for order: {}",
                    event.getOrderId(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.payment-completed}")
    public void handlePaymentCompletedEvent(PaymentCompletedEvent event) {
        log.info("Received PaymentCompletedEvent: eventId={}, orderId={}, transactionId={}",
                event.getEventId(), event.getOrderId(), event.getTransactionId());

        try {
            notificationService.sendPaymentCompletedNotification(
                    event.getOrderId(),
                    event.getCustomerId(),
                    event.getTransactionId(),
                    event.getAmount().toString());
        } catch (Exception e) {
            log.error("Failed to send payment completed notification for order: {}",
                    event.getOrderId(), e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.payment-failed}")
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent: eventId={}, orderId={}, reason={}",
                event.getEventId(), event.getOrderId(), event.getReason());

        try {
            notificationService.sendPaymentFailedNotification(
                    event.getOrderId(),
                    event.getCustomerId(),
                    event.getAmount().toString(),
                    event.getReason());
        } catch (Exception e) {
            log.error("Failed to send payment failed notification for order: {}",
                    event.getOrderId(), e);
        }
    }
}
