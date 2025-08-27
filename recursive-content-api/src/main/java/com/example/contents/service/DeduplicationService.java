package com.example.contents.service;

import com.example.contents.model.Content;
import com.example.contents.repository.ContentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DeduplicationService {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationService.class);

    private final ContentRepository contentRepository;

    public DeduplicationService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }


    @Transactional
    public int deduplicateContents() {
        var dupGroups = contentRepository.findDuplicateGroups();
        AtomicInteger deleted = new AtomicInteger();

        dupGroups.forEach(group -> {
            List<Content> all = contentRepository.findByFileAndTextBlock(group.getFile(), group.getTextBlock());
            if (all.size() <= 1) return;

            // Keep policy: keep the smallest id (first inserted)
            all.sort(Comparator.comparing(Content::getId));
            Content keeper = all.get(0);

            List<Content> toDelete = all.subList(1, all.size());
            int n = toDelete.size();


            contentRepository.deleteAllInBatch(toDelete);
            deleted.addAndGet(n);

            log.info("Deduped group (file='{}', text='{}'): kept id={}, deleted {} row(s)",
                    group.getFile(), group.getTextBlock(), keeper.getId(), n);
        });

        return deleted.get();
    }
}
