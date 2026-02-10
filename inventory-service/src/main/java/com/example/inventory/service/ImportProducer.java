package com.example.inventory.service;

import com.example.inventory.entity.ImportJob;
import com.example.inventory.messaging.dto.ImportChunkEvent;
import com.example.inventory.messaging.dto.ProductDTO;
import com.example.inventory.repository.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ImportJobRepository importJobRepository;

    @Value("${rabbitmq.exchanges.inventory}")
    private String inventoryExchange;

    @Value("${rabbitmq.routing-keys.product-import}")
    private String productImportRoutingKey;

    private static final int CHUNK_SIZE = 1000; // Configurable chunk size

    /**
     * Starts the CSV import process asynchronously.
     * Reads the file line-by-line and publishes chunks to RabbitMQ.
     */
    public String startImport(String filePath) {
        ImportJob job = ImportJob.builder()
                .status(ImportJob.ImportStatus.PROCESSING)
                .startTime(LocalDateTime.now())
                .totalRows(0L)
                .processedRows(0L)
                .failedRows(0L)
                .build();

        ImportJob savedJob = importJobRepository.save(job);

        // Run streaming processing in a separate thread to return response immediately
        CompletableFuture.runAsync(() -> processFile(filePath, savedJob.getId()));

        return savedJob.getId();
    }

    private void processFile(String filePath, String jobId) {
        log.info("Starting CSV processing for job: {} from file: {}", jobId, filePath);
        long totalRows = 0;
        int chunkNumber = 0;
        List<ProductDTO> currentChunk = new ArrayList<>(CHUNK_SIZE);

        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(filePath, StandardCharsets.UTF_8));
                CSVParser csvParser = new CSVParser(reader,
                        CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            for (CSVRecord record : csvParser) {
                totalRows++;
                try {
                    ProductDTO product = parseRecord(record);
                    currentChunk.add(product);

                    if (currentChunk.size() >= CHUNK_SIZE) {
                        publishChunk(jobId, chunkNumber++, currentChunk);
                        currentChunk.clear();
                    }
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", totalRows, e.getMessage());
                    // Log error to DB (omitted for brevity in producer, handled in consumer usually
                    // or separate error queue)
                }
            }

            // Publish remaining rows
            if (!currentChunk.isEmpty()) {
                publishChunk(jobId, chunkNumber, currentChunk);
            }

            // Update total rows
            ImportJob job = importJobRepository.findById(jobId).orElseThrow();
            job.setTotalRows(totalRows);
            importJobRepository.save(job);

            log.info("Finished publishing chunks for job: {}. Total rows: {}", jobId, totalRows);

        } catch (Exception e) {
            log.error("Error processing CSV file for job: {}", jobId, e);
            ImportJob job = importJobRepository.findById(jobId).orElse(null);
            if (job != null) {
                job.setStatus(ImportJob.ImportStatus.FAILED);
                job.setEndTime(LocalDateTime.now());
                importJobRepository.save(job);
            }
        }
    }

    private void publishChunk(String jobId, int chunkNumber, List<ProductDTO> products) {
        ImportChunkEvent event = ImportChunkEvent.builder()
                .jobId(jobId)
                .chunkNumber(chunkNumber)
                .products(new ArrayList<>(products)) // Create copy
                .build();

        rabbitTemplate.convertAndSend(inventoryExchange, productImportRoutingKey, event);
        log.debug("Published chunk {} for job {} with {} products", chunkNumber, jobId, products.size());
    }

    private ProductDTO parseRecord(CSVRecord record) {
        // Index, Name, Description, Brand, Category, Price, Currency, Stock, EAN,
        // Color, Size, Availability, Internal ID
        return ProductDTO.builder()
                .internalId(record.get("Internal ID"))
                .name(record.get("Name"))
                .description(record.get("Description"))
                .brand(record.get("Brand"))
                .category(record.get("Category"))
                .price(new BigDecimal(record.get("Price")))
                .currency(record.get("Currency"))
                .stock(Integer.parseInt(record.get("Stock")))
                .ean(record.get("EAN"))
                .color(record.get("Color"))
                .size(record.get("Size"))
                .availability(record.get("Availability"))
                .build();
    }
}
