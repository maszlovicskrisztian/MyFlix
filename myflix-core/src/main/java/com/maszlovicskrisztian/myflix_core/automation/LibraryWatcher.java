package com.maszlovicskrisztian.myflix_core.automation;

import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.service.LibraryScanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.StandardWatchEventKinds.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryWatcher {

    private final LibraryScanner libraryScanner;
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
        log.info("LibraryWatcher starting, root={}, debounceMinutes={}", root, debounceMinutes);

        registerRecursive(root);
        log.info("LibraryWatcher registered {} directories under {}", keyToPath.size(), root);

        Thread.ofVirtual().name("library-watcher").start(this::watchLoop);
    }

    @PreDestroy
    public void stop() throws IOException {
        log.info("LibraryWatcher stopping");
        running = false;
        watchService.close();
        debounceExecutor.shutdownNow();
    }

    private void registerRecursive(Path start) throws IOException {
        try (var paths = Files.walk(start)) {
            for (Path dir : paths.filter(Files::isDirectory).toList()) {
                try {
                    WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
                    keyToPath.put(key, dir);
                } catch (IOException e) {
                    // pl. inotify watch-limit (fs.inotify.max_user_watches) elérve
                    log.error("Failed to register watch for directory: {}", dir, e);
                }
            }
        }
    }

    private void watchLoop() {
        log.info("Watch loop started on thread {}", Thread.currentThread());
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (ClosedWatchServiceException | InterruptedException e) {
                log.info("Watch loop exiting: {}", e.getClass().getSimpleName());
                break;
            }

            Path dir = keyToPath.get(key);
            if (dir == null) {
                log.warn("Received WatchKey with no known path mapping, skipping: {}", key);
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                if (event.kind() == OVERFLOW) {
                    log.warn("WatchService OVERFLOW on {} — some events may have been lost", dir);
                    scheduleDebouncedScan();
                    continue;
                }

                Path changedName = (Path) event.context();
                Path fullPath = dir.resolve(changedName);
                log.debug("WatchEvent kind={} path={}", event.kind().name(), fullPath);

                if (event.kind() == ENTRY_CREATE && Files.isDirectory(fullPath)) {
                    try {
                        registerRecursive(fullPath);
                        log.info("Registered new subdirectory for watching: {}", fullPath);
                    } catch (IOException e) {
                        log.error("Failed to register new subdirectory: {}", fullPath, e);
                    }
                }

                scheduleDebouncedScan();
            }

            boolean valid = key.reset();
            if (!valid) {
                log.warn("WatchKey no longer valid, removing: {}", dir);
                keyToPath.remove(key);
            }
        }
        log.error("Watch loop thread has exited — LibraryWatcher is no longer active");
    }

    private synchronized void scheduleDebouncedScan() {
        ScheduledFuture<?> existing = pendingTask.get();
        boolean cancelledExisting = existing != null && existing.cancel(false);
        log.debug("Scheduling debounced scan in {} minute(s), previous task cancelled={}",
                debounceMinutes, cancelledExisting);
        pendingTask.set(debounceExecutor.schedule(
                libraryScanner::runScanAndEnrich, debounceMinutes, TimeUnit.MINUTES));
    }
}