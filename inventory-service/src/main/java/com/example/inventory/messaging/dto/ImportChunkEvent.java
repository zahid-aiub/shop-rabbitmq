package com.example.inventory.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportChunkEvent {
    private String jobId;
    private int chunkNumber;
    private List<ProductDTO> products;
}
