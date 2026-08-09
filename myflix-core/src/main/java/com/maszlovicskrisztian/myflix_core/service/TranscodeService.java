package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.HlsSession;
import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import com.maszlovicskrisztian.myflix_core.helpers.HlsSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
@Slf4j
public class TranscodeService {

    private final HlsSessionRegistry sessionRegistry;
    private final ObjectMapper objectMapper;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    @Value("${HLS_PATH}")
    private String hlsBasePath;

    @Value("${ffprobe.path}")
    private String ffprobePath;

    public void touch(Long mediaId) {
        sessionRegistry.touch(mediaId);
    }

    public Path getOrStartSession(Path sourceFile, Long mediaId, long startSeconds) {
        log.trace("FFmpeg session request for file info {} started", mediaId);
        HlsSession existing = sessionRegistry.get(mediaId);
        if (existing != null && existing.startSeconds() != startSeconds) {
            sessionRegistry.discardAndStop(mediaId, getSessionDir(mediaId));
        }

        var session = sessionRegistry.getOrCreate(mediaId, id -> {
            try {
                return startSession(sourceFile, id, startSeconds);
            } catch (IOException e) {
                log.error("Error during session start: {}", e.getMessage());
                throw new UncheckedIOException(e);
            }
        });

        log.trace("FFmpeg session request for file info {} finished", mediaId);
        return session.playlistPath();
    }

    public Path getSessionDir(@NonNull Long mediaId) {
        return Paths.get(hlsBasePath, mediaId.toString());
    }

    public void waitForFirstSegment(Path playlist, Duration timeout, Integer minSegments) throws TimeoutException {
        Path sessionDir = playlist.getParent();
        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            if (Files.exists(playlist) && hasMinSegment(sessionDir, minSegments)) {
                return;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TimeoutException("Waiting for segment cancelled.");
            }
        }

        log.error("Transcoding did not produce {} segment(s) within {}, playlist exists={}", minSegments, timeout, Files.exists(playlist));
        throw new TimeoutException("Transcoding was not ready in time.");
    }

    public MediaProbeResult probe(Path file) throws IOException {
        List<String> command = List.of(
                ffprobePath, "-v", "quiet", "-print_format", "json",
                "-show_format", "-show_streams", file.toString()
        );

        Path probeLog = Files.createTempFile("ffprobe-", ".log");
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectError(probeLog.toFile());

        Process process = pb.start();

        JsonNode root;
        try (InputStream stdout = process.getInputStream()) {
            root = objectMapper.readTree(stdout);
        }

        boolean finished;
        try {
            finished = process.waitFor(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("ffprobe cancelled", e);
        }
        if (!finished || process.exitValue() != 0) {
            process.destroyForcibly();
            log.error("ffprobe failed for {}, exit={}, stderr saved at {}", file, process.exitValue(), probeLog);
            throw new IOException("ffprobe timeout: " + file);
        }

        String videoCodec = findCodec(root, "video");
        String audioCodec = findCodec(root, "audio");
        long durationSeconds = (long)(root.path("format").path("duration").asDouble());
        String container = root.path("format").path("format_name").asString(null);

        return new MediaProbeResult(videoCodec, audioCodec, container, durationSeconds);
    }

    private String findCodec(JsonNode root, String codecType) {
        for (JsonNode stream : root.path("streams")) {
            if (codecType.equals(stream.path("codec_type").asString())) {
                return stream.path("codec_name").asString(null);
            }
        }
        return null;
    }

    private HlsSession startSession(Path sourceFile, Long mediaId, long startSeconds) throws IOException {
        log.trace("Starting FFmpeg session for file info: {}.", mediaId);
        Path sessionDir = Paths.get(hlsBasePath, mediaId.toString());
        Files.createDirectories(sessionDir);
        Path playlistPath = sessionDir.resolve("playlist.m3u8");

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        if (startSeconds > 0) {
            command.add("-ss");
            command.add(String.valueOf(startSeconds));
        }
        command.add("-i");
        command.add(sourceFile.toString());
        command.addAll(List.of(
                "-map", "0:v:0", "-map", "0:a:0",
                "-c:v", "libx264", "-preset", "ultrafast",
                "-c:a", "aac", "-ac", "2",
                "-vsync", "cfr", "-avoid_negative_ts", "make_zero",
                "-start_number", "0", "-hls_time", "4", "-hls_list_size", "0",
                "-hls_playlist_type", "event",
                "-f", "hls", playlistPath.toString()
        ));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(sessionDir.resolve("ffmpeg.log").toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        registerShutdownCleanup(process);

        log.trace("FFmpeg session for file info: {} started.", mediaId);
        return new HlsSession(playlistPath, process, new AtomicReference<>(Instant.now()), startSeconds);
    }

    private void registerShutdownCleanup(Process process) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (process.isAlive()) process.destroyForcibly();
        }));
    }

    private boolean hasMinSegment(Path sessionDir, Integer minSegments) {
        try (Stream<Path> files = Files.list(sessionDir)) {
            return files.filter(p -> p.toString().endsWith(".ts")).count() >= minSegments;
        } catch (IOException e) {
            log.debug("Could not list segment dir {}: {}", sessionDir, e.getMessage());
            return false;
        }
    }
}