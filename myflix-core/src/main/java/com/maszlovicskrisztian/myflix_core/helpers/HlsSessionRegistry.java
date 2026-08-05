package com.maszlovicskrisztian.myflix_core.helpers;

import com.maszlovicskrisztian.myflix_core.dtos.HlsSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

@Component
public class HlsSessionRegistry {

    private final Map<Long, HlsSession> sessions = new ConcurrentHashMap<>();

    public HlsSession get(Long mediaId) {
        return sessions.get(mediaId);
    }

    public HlsSession getOrCreate(Long mediaId, Function<Long, HlsSession> factory) {
        HlsSession session = sessions.computeIfAbsent(mediaId, factory);
        session.lastAccessed().set(Instant.now());
        return session;
    }

    public void touch(Long mediaId) {
        HlsSession session = sessions.get(mediaId);
        if (session != null) session.lastAccessed().set(Instant.now());
    }

    public Set<Map.Entry<Long, HlsSession>> entries() {
        return sessions.entrySet();
    }

    public void discardAndStop(Long mediaId, Path sessionDir) {
        HlsSession removed = sessions.remove(mediaId);
        if (removed == null) return;
        stopProcess(removed.process());
        deleteDirectory(sessionDir);
    }

    private void stopProcess(Process process) {
        if (!process.isAlive()) return;
        process.destroyForcibly();
        try {
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void deleteDirectory(Path dir) {
        if (!Files.exists(dir)) return;
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        } catch (IOException ignored) {}
    }
}