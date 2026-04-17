package com.frameasy.scheduler;

import com.frameasy.service.SchemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Refresh scheme cache every 24 hours.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemeScheduler {

    private final SchemeService schemeService;

    @Scheduled(cron = "0 0 * * * *") // every hour at minute 0; use "0 0 0 * * *" for daily at midnight
    public void refreshSchemes() {
        log.info("Refreshing schemes cache");
        int count = schemeService.fetchAndCache();
        log.info("Schemes refreshed: {} entries", count);
    }
}
