package com.example.contents.controller;

import com.example.contents.dto.BulkJobStatus;
import com.example.contents.dto.ContentDto;
import com.example.contents.service.BulkJobRegistry;
import com.example.contents.service.ContentBulkService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    @Autowired
    private com.example.contents.service.ContentService service;

    @Autowired
    private ContentBulkService bulkService;

    @Autowired
    private BulkJobRegistry jobs;


    @Operation(summary = "Start async bulk insert; returns jobId to poll")
    @PostMapping("/bulk/async")
    public ResponseEntity<BulkJobStatus> bulkInsertAsync(
            @RequestBody @Valid List<UpsertRequest> payload,
            @RequestParam(defaultValue = "1000") int chunkSize,
            @RequestParam(defaultValue = "false") boolean dedupePayload,
            @RequestParam(defaultValue = "false") boolean dedupeDb
    ) {
        List<ContentDto> dtos = payload.stream()
                .map(req -> ContentDto.builder()
                        .File(req.File)
                        .textBlock(req.textBlock)
                        .build())
                .toList();

        BulkJobStatus job = jobs.create(dtos.size());
        bulkService.bulkInsertAsync(job.getJobId(), dtos, chunkSize, dedupePayload, dedupeDb);


        URI statusUri = URI.create("/api/contents/bulk/jobs/" + job.getJobId());
        return ResponseEntity.accepted()
                .location(statusUri)
                .body(job);
    }

    @Operation(summary = "Get async bulk job status")
    @GetMapping("/bulk/jobs/{jobId}")
    public ResponseEntity<BulkJobStatus> getBulkJob(@PathVariable UUID jobId) {
        BulkJobStatus st = jobs.get(jobId);
        return (st == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(st);
    }


    @Data
    public static class UpsertRequest {
        public Long id; // ignored in bulk
        @NotBlank public String File;
        @NotBlank public String textBlock;
    }
}
