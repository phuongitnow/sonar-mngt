package com.sonarqube.adminapp.infrastructure.scheduler;

import com.sonarqube.adminapp.application.service.CleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {
    
    private final CleanupService cleanupService;
    
    @Scheduled(cron = "${app.cleanup.cron}")
    public void scheduledCleanup() {
        log.info("Starting scheduled cleanup task...");
        try {
            cleanupService.performCleanup();
            log.info("Scheduled cleanup completed successfully");
        } catch (Exception e) {
            log.error("Scheduled cleanup failed", e);
        }
    }
}

