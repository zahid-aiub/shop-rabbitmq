package com.example.inventory.service;

import com.example.inventory.entity.InventoryItem;
import com.example.inventory.messaging.dto.InventoryFailedEvent;
import com.example.inventory.messaging.dto.InventoryReservedEvent;
import com.example.inventory.messaging.dto.OrderCreatedEvent;
import com.example.inventory.repository.InventoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @PostConstruct
    public void initializeInventory() {
        // Initialize some sample inventory items for testing
        if (inventoryRepository.count() == 0) {
            log.info("Initializing sample inventory items...");

            InventoryItem laptop = new InventoryItem();
            laptop.setProductId("PROD-001");
            laptop.setProductName("Laptop");
            laptop.setAvailableQuantity(10);
            laptop.setReservedQuantity(0);
            inventoryRepository.save(laptop);

            InventoryItem mouse = new InventoryItem();
            mouse.setProductId("PROD-002");
            mouse.setProductName("Mouse");
            mouse.setAvailableQuantity(50);
            mouse.setReservedQuantity(0);
            inventoryRepository.save(mouse);

            InventoryItem keyboard = new InventoryItem();
            keyboard.setProductId("PROD-003");
            keyboard.setProductName("Keyboard");
            keyboard.setAvailableQuantity(30);
            keyboard.setReservedQuantity(0);
            inventoryRepository.save(keyboard);

            log.info("Sample inventory initialized successfully");
        }
    }

    @Transactional
    public InventoryReservedEvent processOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Processing OrderCreatedEvent for order: {}", event.getOrderId());

        List<InventoryReservedEvent.ReservedItemDTO> reservedItems = new ArrayList<>();

        // Check and reserve inventory for each item
        for (OrderCreatedEvent.OrderItemDTO item : event.getItems()) {
            InventoryItem inventoryItem = inventoryRepository.findByProductId(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            if (!inventoryItem.hasAvailableStock(item.getQuantity())) {
                log.warn("Insufficient inventory for product: {} (requested: {}, available: {})",
                        item.getProductId(), item.getQuantity(), inventoryItem.getAvailableQuantity());
                throw new RuntimeException("Insufficient stock for product: " + item.getProductId());
            }

            // Reserve the stock
            inventoryItem.reserveStock(item.getQuantity());
            inventoryRepository.save(inventoryItem);

            log.info("Reserved {} units of product: {}", item.getQuantity(), item.getProductId());

            reservedItems.add(new InventoryReservedEvent.ReservedItemDTO(
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getPrice()));
        }

        log.info("Successfully reserved inventory for order: {}", event.getOrderId());

        return new InventoryReservedEvent(
                event.getOrderId(),
                event.getCustomerId(),
                reservedItems,
                event.getTotalAmount());
    }

    public InventoryFailedEvent createInventoryFailedEvent(OrderCreatedEvent event, String reason,
            String failedProductId) {
        log.error("Inventory reservation failed for order: {} - Reason: {}", event.getOrderId(), reason);

        return new InventoryFailedEvent(
                event.getOrderId(),
                event.getCustomerId(),
                reason,
                failedProductId);
    }
}
