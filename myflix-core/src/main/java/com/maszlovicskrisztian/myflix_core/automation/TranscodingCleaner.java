package com.maszlovicskrisztian.myflix_core.automation;

import com.maszlovicskrisztian.myflix_core.helpers.HlsSessionRegistry;
import com.maszlovicskrisztian.myflix_core.service.TranscodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
public class TranscodingCleaner {

    private final HlsSessionRegistry sessionRegistry;
    private final TranscodeService transcodeService;

    @Value("${HLS_IDLE_TIMEOUT_MINUTES:5}")
    private int idleTimeoutMinutes;

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void cleanupIdleSessions() {
        Instant idleThreshold = Instant.now().minus(Duration.ofMinutes(idleTimeoutMinutes));

        sessionRegistry.entries().stream()
                .filter(entry -> entry.getValue().lastAccessed().get().isBefore(idleThreshold))
                .map(Map.Entry::getKey)
                .toList()
                .forEach(mediaId -> sessionRegistry.discardAndStop(mediaId, transcodeService.getSessionDir(mediaId)));
    }
}
