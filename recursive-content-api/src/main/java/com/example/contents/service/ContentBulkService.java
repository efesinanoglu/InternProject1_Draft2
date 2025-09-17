package com.example.contents.service;

import com.example.contents.dto.BulkInsertResponse;
import com.example.contents.dto.ContentDto;
import com.example.contents.model.Content;
import com.example.contents.repository.ContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Inserts Content rows in chunks with flush/clear to keep memory small.
 * Optional de-duplication:
 *  - dedupePayload: skips duplicates within the same request (case/trim-insensitive).
 *  - dedupeDb: skips rows that already exist in DB by (file, textBlock), case-insensitive.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentBulkService {

    private final ContentRepository repo;
    private final EntityManager em;

    @Transactional
    public BulkInsertResponse bulkInsert(
            @NotNull List<ContentDto> dtos,
            int chunkSize,
            boolean dedupePayload,
            boolean dedupeDb
    ) {
        if (chunkSize <= 0) chunkSize = 1000;

        List<Long> createdIds = new ArrayList<>(dtos.size());
        int skipped = 0;

        // For payload-level de-duplication (case/trim-insensitive key)
        Set<String> seen = dedupePayload ? new HashSet<>(dtos.size() * 2) : Collections.emptySet();

        int i = 0;
        for (ContentDto dto : dtos) {
            String file = dto.getFile();
            String text = dto.getTextBlock();

            // Basic sanity: controller already @NotBlank, but double check
            if (file == null || text == null) {
                skipped++;
                continue;
            }

            if (dedupePayload) {
                String key = norm(file) + "||" + norm(text);
                if (!seen.add(key)) {
                    skipped++;
                    continue; // duplicate within this request
                }
            }

            if (dedupeDb) {
                boolean exists = repo.existsByFileIgnoreCaseAndTextBlockIgnoreCase(file, text);
                if (exists) {
                    skipped++;
                    continue; // already in DB
                }
            }

            Content c = new Content();
            c.setFile(file);
            c.setTextBlock(text);

            repo.save(c);              // Hibernate will queue insert
            createdIds.add(c.getId());
            i++;

            // Periodic flush/clear to bound persistence context size
            if (i % chunkSize == 0) {
                em.flush();
                em.clear();
            }
        }

        // Final flush
        em.flush();
        em.clear();

        return new BulkInsertResponse(createdIds.size(), skipped, createdIds);
    }

    private static String norm(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}
