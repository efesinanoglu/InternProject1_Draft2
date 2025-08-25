package com.example.contents.bootstrap;

import com.example.contents.service.DeduplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dedup-on-start") // only runs when this profile is active
@Component
public class DeduplicationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationRunner.class);

    private final DeduplicationService service;

    public DeduplicationRunner(DeduplicationService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        int removed = service.deduplicateContents();
        log.info("Startup dedup finished. Removed {} duplicate row(s).", removed);
    }
}
