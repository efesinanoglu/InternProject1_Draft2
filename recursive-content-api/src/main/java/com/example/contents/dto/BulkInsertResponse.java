package com.example.contents.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class BulkInsertResponse {
    private int createdCount;
    private int skippedCount;       // duplicates skipped (payload and/or DB)
    private List<Long> createdIds;  // ids of inserted rows
}
