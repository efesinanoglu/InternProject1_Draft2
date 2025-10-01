package com.example.contents.service;

import com.example.contents.model.Content;
import com.example.contents.repository.IContentRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentChunkWriter {

    private final IContentRepository repo;
    private final EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int writeChunk(List<Content> batch) {
        repo.saveAll(batch);
        em.flush();
        em.clear();
        return batch.size();
    }
}
