package com.example.contents.service;

import com.example.contents.dto.BulkJobStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BulkJobRegistry {

    private final Map<UUID, BulkJobStatus> jobs = new ConcurrentHashMap<>();

    public BulkJobStatus create(int total) {
        UUID id = UUID.randomUUID();
        BulkJobStatus st = BulkJobStatus.builder()
                .jobId(id)
                .state(BulkJobStatus.State.QUEUED)
                .total(total)
                .processed(0)
                .created(0)
                .skipped(0)
                .submittedAt(Instant.now())
                .build();
        jobs.put(id, st);
        return st;
    }

    public BulkJobStatus get(UUID id) {
        return jobs.get(id);
    }

    public void markRunning(UUID id) {
        var st = jobs.get(id);
        if (st != null) {
            st.setState(BulkJobStatus.State.RUNNING);
            st.setStartedAt(Instant.now());
        }
    }

    public void progress(UUID id, int processedDelta, int createdDelta, int skippedDelta) {
        var st = jobs.get(id);
        if (st != null) {
            st.setProcessed(st.getProcessed() + processedDelta);
            st.setCreated(st.getCreated() + createdDelta);
            st.setSkipped(st.getSkipped() + skippedDelta);
        }
    }

    public void markSucceeded(UUID id) {
        var st = jobs.get(id);
        if (st != null) {
            st.setState(BulkJobStatus.State.SUCCEEDED);
            st.setFinishedAt(Instant.now());
        }
    }

    public void markFailed(UUID id, String message) {
        var st = jobs.get(id);
        if (st != null) {
            st.setState(BulkJobStatus.State.FAILED);
            st.setError(message);
            st.setFinishedAt(Instant.now());
        }
    }
}
