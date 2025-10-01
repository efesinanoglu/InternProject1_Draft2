package com.example.contents.scheduler;

import com.example.contents.model.Content;
import com.example.contents.repository.IContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ContentScheduler {

    private static final Logger log = LoggerFactory.getLogger(ContentScheduler.class);

    @Autowired
    IContentRepository contentRepository;

    @PersistenceContext
    private EntityManager em;

    // Runs at every 15 seconds (Europe/Istanbul)
    @Scheduled(cron = "*/15 * * * * *", zone = "Europe/Istanbul")
    @Transactional
    public void pendingContent() {
        int dedup = handleDuplicates();
        int processed = handlePendingBatch();

        if (dedup == 0 && processed == 0) {
            log.debug("ContentScheduler: nothing to do this run.");
        } else {
            log.info("ContentScheduler: dedup={}, processed={}", dedup, processed);
        }
    }

    private int handleDuplicates() {
        List<IContentRepository.DupGroup> groups = contentRepository.findDuplicateGroups();
        if (groups.isEmpty()) return 0;

        int affected = 0;
        for (IContentRepository.DupGroup g : groups) {
            String file = g.getFile() == null ? null : g.getFile().trim();
            String text = g.getTextBlock() == null ? null : g.getTextBlock().trim();

            List<Content> dupes = em.createNativeQuery("""
                     SELECT *
                     FROM contents
                        WHERE lower(btrim(coalesce(file::text, ''))) = :file
                            AND lower(btrim(coalesce(text_block::text, ''))) = :text
                                ORDER BY id ASC
                                            """, Content.class)
                    .setParameter("file", g.getFile())
                    .setParameter("text", g.getTextBlock())
                    .getResultList();

            if (dupes.size() <= 1) continue;

            dupes.sort(Comparator.comparingLong(Content::getId));
            Content keeper = dupes.get(0);

            LocalDateTime now = LocalDateTime.now();
            for (int i = 1; i < dupes.size(); i++) {
                Content d = dupes.get(i);
                d.setProcessed(true);
                d.setProcessedAt(now);
            }
            contentRepository.saveAll(dupes.subList(1, dupes.size()));

            affected += (dupes.size() - 1);
            log.info("De-dup: kept id={}, marked {} duplicate(s) for file='{}'",
                    keeper.getId(), dupes.size() - 1, file);
        }
        return affected;
    }

    private int handlePendingBatch() {
        List<Content> batch = contentRepository.findTop100ByProcessedFalseOrderByIdAsc();

        if (batch.isEmpty()) return 0;

        LocalDateTime now = LocalDateTime.now();
        for (Content c : batch) {
            c.setProcessed(true);
            c.setProcessedAt(now);
        }
        contentRepository.saveAll(batch);
        return batch.size();
    }
}
