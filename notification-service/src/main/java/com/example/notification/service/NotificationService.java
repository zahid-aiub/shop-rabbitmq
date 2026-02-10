package com.example.notification.service;

import com.example.notification.entity.NotificationLog;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendInventoryFailedNotification(Long orderId, String customerId, String reason,
            String failedProductId) {
        log.info("Sending inventory failed notification for order: {}", orderId);

        String message = String.format(
                "Order #%d - Inventory Check Failed\n" +
                        "Unfortunately, we cannot fulfill your order due to insufficient stock.\n" +
                        "Product: %s\n" +
                        "Reason: %s\n" +
                        "We apologize for the inconvenience.",
                orderId, failedProductId, reason);

        NotificationLog notification = new NotificationLog();
        notification.setEventType("INVENTORY_FAILED");
        notification.setOrderId(orderId);
        notification.setCustomerId(customerId);
        notification.setMessage(message);
        notification.setRecipientInfo(customerId + "@example.com");
        notification.setChannel(NotificationLog.NotificationChannel.EMAIL);
        notification.setStatus(NotificationLog.NotificationStatus.SENT);

        notificationRepository.save(notification);

        // Simulate email sending
        log.info("📧 EMAIL NOTIFICATION SENT:");
        log.info("To: {}", notification.getRecipientInfo());
        log.info("Subject: Order #{} - Inventory Check Failed", orderId);
        log.info("Message: {}", message);
        log.info("=".repeat(80));
    }

    @Transactional
    public void sendPaymentCompletedNotification(Long orderId, String customerId, String transactionId, String amount) {
        log.info("Sending payment completed notification for order: {}", orderId);

        String message = String.format(
                "Order #%d - Payment Successful! 🎉\n" +
                        "Your payment has been processed successfully.\n" +
                        "Transaction ID: %s\n" +
                        "Amount: $%s\n" +
                        "Your order is being prepared for shipment.\n" +
                        "Thank you for your purchase!",
                orderId, transactionId, amount);

        NotificationLog notification = new NotificationLog();
        notification.setEventType("PAYMENT_COMPLETED");
        notification.setOrderId(orderId);
        notification.setCustomerId(customerId);
        notification.setMessage(message);
        notification.setRecipientInfo(customerId + "@example.com");
        notification.setChannel(NotificationLog.NotificationChannel.EMAIL);
        notification.setStatus(NotificationLog.NotificationStatus.SENT);

        notificationRepository.save(notification);

        // Simulate email sending
        log.info("📧 EMAIL NOTIFICATION SENT:");
        log.info("To: {}", notification.getRecipientInfo());
        log.info("Subject: Order #{} - Payment Successful!", orderId);
        log.info("Message: {}", message);
        log.info("=".repeat(80));

        // Also send SMS notification
        sendSMSNotification(orderId, customerId, transactionId);
    }

    @Transactional
    public void sendPaymentFailedNotification(Long orderId, String customerId, String amount, String reason) {
        log.info("Sending payment failed notification for order: {}", orderId);

        String message = String.format(
                "Order #%d - Payment Failed\n" +
                        "We were unable to process your payment.\n" +
                        "Amount: $%s\n" +
                        "Reason: %s\n" +
                        "Please try again or contact customer support.",
                orderId, amount, reason);

        NotificationLog notification = new NotificationLog();
        notification.setEventType("PAYMENT_FAILED");
        notification.setOrderId(orderId);
        notification.setCustomerId(customerId);
        notification.setMessage(message);
        notification.setRecipientInfo(customerId + "@example.com");
        notification.setChannel(NotificationLog.NotificationChannel.EMAIL);
        notification.setStatus(NotificationLog.NotificationStatus.SENT);

        notificationRepository.save(notification);

        // Simulate email sending
        log.info("📧 EMAIL NOTIFICATION SENT:");
        log.info("To: {}", notification.getRecipientInfo());
        log.info("Subject: Order #{} - Payment Failed", orderId);
        log.info("Message: {}", message);
        log.info("=".repeat(80));
    }

    private void sendSMSNotification(Long orderId, String customerId, String transactionId) {
        String smsMessage = String.format(
                "Your order #%d payment was successful! Transaction: %s. Thank you!",
                orderId, transactionId);

        NotificationLog smsLog = new NotificationLog();
        smsLog.setEventType("PAYMENT_COMPLETED");
        smsLog.setOrderId(orderId);
        smsLog.setCustomerId(customerId);
        smsLog.setMessage(smsMessage);
        smsLog.setRecipientInfo("+1-XXX-XXX-" + customerId.hashCode() % 10000);
        smsLog.setChannel(NotificationLog.NotificationChannel.SMS);
        smsLog.setStatus(NotificationLog.NotificationStatus.SENT);

        notificationRepository.save(smsLog);

        // Simulate SMS sending
        log.info("📱 SMS NOTIFICATION SENT:");
        log.info("To: {}", smsLog.getRecipientInfo());
        log.info("Message: {}", smsMessage);
        log.info("=".repeat(80));
    }
}
