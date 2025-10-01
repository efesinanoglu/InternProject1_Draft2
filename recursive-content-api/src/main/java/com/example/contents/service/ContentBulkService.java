package com.example.contents.service;

import com.example.contents.dto.ContentDto;
import com.example.contents.model.Content;
import com.example.contents.repository.IContentRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ContentBulkService {

    private final IContentRepository repo;
    private final ContentChunkWriter chunkWriter;
    private final BulkJobRegistry jobs;

    @Async("bulkExecutor")
    public CompletableFuture<Void> bulkInsertAsync(
            UUID jobId,
            List<ContentDto> dtos,
            int chunkSize,
            boolean dedupePayload,
            boolean dedupeDb
    ) {
        jobs.markRunning(jobId);

        try {
            if (chunkSize <= 0) chunkSize = 1000;

            Set<String> seen = dedupePayload ? new HashSet<>(dtos.size() * 2) : Collections.emptySet();

            List<Content> batch = new ArrayList<>(chunkSize);
            int processedSinceLastUpdate = 0;
            int createdSinceLastUpdate   = 0;
            int skippedSinceLastUpdate   = 0;

            for (ContentDto dto : dtos) {
                String file = dto.getFile();
                String text = dto.getTextBlock();

                if (isBlank(file) || isBlank(text)) {
                    skippedSinceLastUpdate++;
                    processedSinceLastUpdate++;
                    continue;
                }

                if (dedupePayload) {
                    String key = norm(file) + "||" + norm(text);
                    if (!seen.add(key)) {
                        skippedSinceLastUpdate++;
                        processedSinceLastUpdate++;
                        continue;
                    }
                }

                if (dedupeDb && repo.existsCaseInsensitive(file, text)) {
                    skippedSinceLastUpdate++;
                    processedSinceLastUpdate++;
                    continue;
                }

                Content c = new Content();
                c.setFile(file);
                c.setTextBlock(text);
                batch.add(c);

                if (batch.size() >= chunkSize) {
                    int wrote = chunkWriter.writeChunk(batch);
                    createdSinceLastUpdate += wrote;
                    processedSinceLastUpdate += wrote;
                    batch.clear();

                    jobs.progress(jobId, processedSinceLastUpdate, createdSinceLastUpdate, skippedSinceLastUpdate);
                    processedSinceLastUpdate = createdSinceLastUpdate = skippedSinceLastUpdate = 0;
                }
            }

            if (!batch.isEmpty()) {
                int wrote = chunkWriter.writeChunk(batch);
                createdSinceLastUpdate += wrote;
                processedSinceLastUpdate += wrote;
                batch.clear();
            }

            if (processedSinceLastUpdate + createdSinceLastUpdate + skippedSinceLastUpdate > 0) {
                jobs.progress(jobId, processedSinceLastUpdate, createdSinceLastUpdate, skippedSinceLastUpdate);
            }

            jobs.markSucceeded(jobId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception ex) {
            jobs.markFailed(jobId, ex.getMessage());
            return CompletableFuture.failedFuture(ex);
        }
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String norm(String s) {
        return s.trim().toLowerCase(Locale.ROOT);
    }
}
