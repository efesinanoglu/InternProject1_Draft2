package com.example.contents.dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkJobStatus {

    public enum State { QUEUED, RUNNING, SUCCEEDED, FAILED }

    private UUID jobId;
    private State state;

    private int total;       // total items in request
    private int processed;   // processed so far (includes skipped)
    private int created;     // successfully inserted
    private int skipped;     // duplicates/invalids skipped

    private String error;    // last error message if failed

    private Instant submittedAt;
    private Instant startedAt;
    private Instant finishedAt;
}
