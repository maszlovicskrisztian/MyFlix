package com.maszlovicskrisztian.myflix_core.automation;

import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.service.LibraryScanner;
import com.maszlovicskrisztian.myflix_core.service.MediaMetadataService;
import lombok.RequiredArgsConstructor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.*;

@Component
@RequiredArgsConstructor
public class LibraryWatcher {

    private final LibraryScanner libraryScanner;
    private final MediaMetadataService mediaMetadataService;
    private final MediaPathResolver mediaPathResolver;

    @Value("${WATCHER_DEBOUNCE_MINUTES}")
    private int debounceMinutes;

    private WatchService watchService;
    private final Map<WatchKey, Path> keyToPath = new ConcurrentHashMap<>();
    private final ScheduledExecutorService debounceExecutor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<ScheduledFuture<?>> pendingTask = new AtomicReference<>();
    private volatile boolean running = true;

    @PostConstruct
    public void start() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        Path root = mediaPathResolver.getMediaPath();
        registerRecursive(root);

        Thread.ofVirtual().name("library-watcher").start(this::watchLoop);
    }

    @PreDestroy
    public void stop() throws IOException {
        running = false;
        watchService.close();
        debounceExecutor.shutdownNow();
    }

    private void registerRecursive(Path start) throws IOException {
        try (var paths = Files.walk(start)) {
            for (Path dir : paths.filter(Files::isDirectory).toList()) {
                WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
                keyToPath.put(key, dir);
            }
        }
    }

    private void watchLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (ClosedWatchServiceException | InterruptedException e) {
                break;
            }

            Path dir = keyToPath.get(key);
            if (dir == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() == OVERFLOW) {
                    scheduleDebouncedScan();
                    continue;
                }

                Path changedName = (Path) event.context();
                Path fullPath = dir.resolve(changedName);

                if (event.kind() == ENTRY_CREATE && Files.isDirectory(fullPath)) {
                    try {
                        registerRecursive(fullPath);
                    } catch (IOException e) {
                        // logoljuk majd
                    }
                }

                scheduleDebouncedScan();
            }

            boolean valid = key.reset();
            if (!valid) {
                keyToPath.remove(key);
            }
        }
    }

    private synchronized void scheduleDebouncedScan() {
        ScheduledFuture<?> existing = pendingTask.get();
        if (existing != null) {
            existing.cancel(false);
        }
        pendingTask.set(debounceExecutor.schedule(
                this::runScanAndEnrich, debounceMinutes, TimeUnit.MINUTES));
    }

    private void runScanAndEnrich() {
        try {
            List<FileInfo> newFiles = libraryScanner.scanAndSave();
            mediaMetadataService.enrichMedias(newFiles);
        } catch (IOException e) {
            // logoljuk majd
        }
    }
}