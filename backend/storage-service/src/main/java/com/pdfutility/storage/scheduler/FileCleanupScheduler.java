package com.pdfutility.storage.scheduler;

import com.pdfutility.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for cleaning up expired temporary files.
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final FileStorageService fileStorageService;

    /**
     * Run cleanup every hour.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupExpiredFiles() {
        log.info("Starting expired file cleanup...");
        
        fileStorageService.cleanupExpiredFiles()
                .doOnNext(fileId -> log.debug("Cleaned up file: {}", fileId))
                .count()
                .doOnSuccess(count -> log.info("Cleanup completed. Removed {} expired files.", count))
                .subscribe();
    }
}
