package com.example.inventory.controller;

import com.example.inventory.entity.ImportJob;
import com.example.inventory.repository.ImportJobRepository;
import com.example.inventory.service.ImportProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {

    private final ImportProducer importProducer;
    private final ImportJobRepository importJobRepository;

    @GetMapping("/test")
    public String testApi() {
        return "Test from Inventory service ...";
    }


    @PostMapping
    public ResponseEntity<String> importProducts(
            @RequestParam(value = "fileName", defaultValue = "products-2000000.csv") String fileName) {
        // Since we mounted ./:/app/data, the file should be at /app/data/{fileName}
        String filePath = "/Users/zahid/Projects/AI/shoping/" + fileName;
        String jobId = importProducer.startImport(filePath);
        return ResponseEntity.accepted().body("Import started for file: " + filePath + ". Job ID: " + jobId);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ImportJob> getImportStatus(@PathVariable String jobId) {
        return importJobRepository.findById(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
