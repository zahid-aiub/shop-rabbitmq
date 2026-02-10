package com.example.inventory.service;

import com.example.inventory.entity.ImportError;
import com.example.inventory.entity.ImportJob;
import com.example.inventory.entity.Product;
import com.example.inventory.messaging.dto.ImportChunkEvent;
import com.example.inventory.messaging.dto.ProductDTO;
import com.example.inventory.repository.ImportErrorRepository;
import com.example.inventory.repository.ImportJobRepository;
import com.example.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportConsumer {

    private final ProductRepository productRepository;
    private final ImportJobRepository importJobRepository;
    private final ImportErrorRepository importErrorRepository;

    @RabbitListener(queues = "${rabbitmq.queues.product-import}", concurrency = "5-10")
    public void consumeChunk(ImportChunkEvent event) {
        log.info("Processing chunk {} for job {} with {} products", event.getChunkNumber(), event.getJobId(),
                event.getProducts().size());

        List<Product> productsToSave = new ArrayList<>();
        List<ImportError> errors = new ArrayList<>();

        for (ProductDTO dto : event.getProducts()) {
            try {
                Product product = mapToEntity(dto);
                productsToSave.add(product);
            } catch (Exception e) {
                errors.add(ImportError.builder()
                        .jobId(event.getJobId())
                        .errorMessage(e.getMessage())
                        .rowData(dto.toString())
                        .build());
            }
        }

        // Batch Insert with Fallback
        int successCount = 0;
        int failureCount = 0;

        if (!productsToSave.isEmpty()) {
            try {
                productRepository.saveAll(productsToSave);
                successCount = productsToSave.size();
            } catch (Exception e) {
                log.warn("Batch insert failed for job {}, falling back to row-by-row processing. Error: {}",
                        event.getJobId(), e.getMessage());
                // Fallback to row-by-row
                for (Product product : productsToSave) {
                    try {
                        productRepository.save(product);
                        successCount++;
                    } catch (Exception ex) {
                        failureCount++;
                        String errorMsg = ex.getMessage();
                        if (errorMsg != null && errorMsg.length() > 1000) {
                            errorMsg = errorMsg.substring(0, 1000);
                        }
                        errors.add(ImportError.builder()
                                .jobId(event.getJobId())
                                .errorMessage(errorMsg)
                                .rowData("InternalID: " + product.getInternalId())
                                .build());
                    }
                }
            }
        }

        // Save Errors
        if (!errors.isEmpty()) {
            try {
                importErrorRepository.saveAll(errors);
            } catch (Exception e) {
                log.error("Failed to save import errors for job {}", event.getJobId(), e);
            }
        }

        // Update Job Progress Atomically
        // Update Job Progress Atomically
        importJobRepository.updateProgress(event.getJobId(), successCount, errors.size());

        // Check for completion (this might still be racy for the status update, but
        // better)
        checkAndCompleteJob(event.getJobId());
    }

    private void checkAndCompleteJob(String jobId) {
        ImportJob job = importJobRepository.findById(jobId).orElse(null);
        if (job != null && job.getTotalRows() > 0 &&
                (job.getProcessedRows() + job.getFailedRows() >= job.getTotalRows())) {

            // Double check to avoid premature completion if totalRows is not yet set
            // (though producer sets it at end)
            // But here we rely on eventual consistency.
            // A better approach would be a separate scheduled task or event when producer
            // finishes.
            // For now, we just check if we reached the total.

            if (job.getStatus() != ImportJob.ImportStatus.COMPLETED) {
                job.setStatus(ImportJob.ImportStatus.COMPLETED);
                job.setEndTime(LocalDateTime.now());
                importJobRepository.save(job);
            }
        }
    }

    private Product mapToEntity(ProductDTO dto) {
        return Product.builder()
                .internalId(dto.getInternalId())
                .name(dto.getName())
                .description(dto.getDescription())
                .brand(dto.getBrand())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .currency(dto.getCurrency())
                .stock(dto.getStock())
                .ean(dto.getEan())
                .color(dto.getColor())
                .size(dto.getSize())
                .availability(dto.getAvailability())
                .build();
    }
}
