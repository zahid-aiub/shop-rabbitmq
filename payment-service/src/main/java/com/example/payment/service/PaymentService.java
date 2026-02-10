package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentTransaction;
import com.example.payment.messaging.dto.InventoryReservedEvent;
import com.example.payment.messaging.dto.PaymentCompletedEvent;
import com.example.payment.messaging.dto.PaymentFailedEvent;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final Random random = new Random();

    @Transactional
    public PaymentCompletedEvent processPayment(InventoryReservedEvent event) {
        log.info("Processing payment for order: {}", event.getOrderId());

        // Create payment record
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setCustomerId(event.getCustomerId());
        payment.setAmount(event.getTotalAmount());
        payment.setPaymentMethod("CREDIT_CARD");
        payment.setStatus(Payment.PaymentStatus.PROCESSING);

        Payment savedPayment = paymentRepository.save(payment);

        // Create transaction record
        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentId(savedPayment.getId());
        transaction.setTransactionId(transactionId);
        transaction.setAmount(event.getTotalAmount());
        transaction.setGateway("STRIPE");
        transaction.setStatus(PaymentTransaction.TransactionStatus.INITIATED);

        transactionRepository.save(transaction);

        // Simulate payment processing (80% success rate for demo)
        boolean paymentSuccess = random.nextInt(100) < 80;

        if (paymentSuccess) {
            // Update transaction status
            transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCESS);
            transaction.setResponseCode("00");
            transaction.setResponseMessage("Payment successful");
            transactionRepository.save(transaction);

            // Update payment status
            savedPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            savedPayment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(savedPayment);

            log.info("Payment successful for order: {}, transactionId: {}",
                    event.getOrderId(), transactionId);

            return new PaymentCompletedEvent(
                    event.getOrderId(),
                    event.getCustomerId(),
                    savedPayment.getId(),
                    transactionId,
                    event.getTotalAmount(),
                    "CREDIT_CARD");
        } else {
            // Payment failed - simulate error
            String errorCode = "ERR_" + random.nextInt(1000);
            String failureReason = "Insufficient funds in customer account";

            transaction.setStatus(PaymentTransaction.TransactionStatus.FAILED);
            transaction.setResponseCode(errorCode);
            transaction.setResponseMessage(failureReason);
            transactionRepository.save(transaction);

            savedPayment.setStatus(Payment.PaymentStatus.FAILED);
            savedPayment.setFailureReason(failureReason);
            paymentRepository.save(savedPayment);

            log.warn("Payment failed for order: {}, reason: {}", event.getOrderId(), failureReason);

            throw new PaymentFailedException(failureReason, errorCode);
        }
    }

    public PaymentFailedEvent createPaymentFailedEvent(InventoryReservedEvent event, String reason, String errorCode) {
        return new PaymentFailedEvent(
                event.getOrderId(),
                event.getCustomerId(),
                event.getTotalAmount(),
                reason,
                errorCode);
    }

    public static class PaymentFailedException extends RuntimeException {
        private final String errorCode;

        public PaymentFailedException(String message, String errorCode) {
            super(message);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
