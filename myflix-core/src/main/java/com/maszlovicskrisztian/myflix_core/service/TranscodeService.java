package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.HlsSession;
import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import com.maszlovicskrisztian.myflix_core.dtos.enums.ExceptionReason;
import com.maszlovicskrisztian.myflix_core.exception.MediaProcessingException;
import com.maszlovicskrisztian.myflix_core.helpers.HlsSessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    @Value("${FFMPEG_HW_ACCEL:none}")
    private String hwAccel;

    @Value("${VAAPI_DEVICE:/dev/dri/renderD128}")
    private String vaapiDevice;

    public void touch(Long mediaId) {
        sessionRegistry.touch(mediaId);
    }

    public Path getOrStartSession(Path sourceFile, Long mediaId, long startSeconds, Integer resHeight) {
        log.trace("FFmpeg session request for file info {} started", mediaId);
        HlsSession existing = sessionRegistry.get(mediaId);
        if (existing != null && existing.startSeconds() != startSeconds) {
            sessionRegistry.discardAndStop(mediaId, getSessionDir(mediaId));
        }

        var session = sessionRegistry.getOrCreate(mediaId, id -> {
            try {
                return startSession(sourceFile, id, startSeconds, resHeight);
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

    public void waitForFirstSegment(Path playlist, Duration timeout, Integer minSegments) {
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
                throw new MediaProcessingException("Waiting for segment cancelled.", ExceptionReason.INTERRUPTED, e);
            }
        }

        log.error("Transcoding did not produce {} segment(s) within {}, playlist exists={}", minSegments, timeout, Files.exists(playlist));
        throw new MediaProcessingException("Transcoding was not ready in time.", ExceptionReason.TIMEOUT, null);
    }

    public MediaProbeResult probe(Path file) {
        List<String> command = List.of(
                ffprobePath, "-v", "quiet", "-print_format", "json",
                "-show_format", "-show_streams", file.toString()
        );

        boolean finished;
        JsonNode root;
        Process process;
        Path probeLog;
        try {
            probeLog = Files.createTempFile("ffprobe-", ".log");
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectError(probeLog.toFile());
            process = pb.start();
            root = objectMapper.readTree(process.getInputStream());
            finished = process.waitFor(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MediaProcessingException("ffprobe cancelled", ExceptionReason.INTERRUPTED, e);
        } catch (IOException e) {
            throw new MediaProcessingException("ffprobe I/O error for " + file, ExceptionReason.IO_ERROR, e);
        }

        if (!finished || process.exitValue() != 0) {
            process.destroyForcibly();
            throw new MediaProcessingException("ffprobe timed out or failed for " + file, ExceptionReason.TIMEOUT, null);
        }

        String videoCodec = findCodec(root, "video");
        String audioCodec = findCodec(root, "audio");
        String container = root.path("format").path("format_name").asString(null);
        long durationSeconds = (long)(root.path("format").path("duration").asDouble());
        Integer height = findHeight(root);

        return new MediaProbeResult(videoCodec, audioCodec, container, durationSeconds, height);
    }

    private Integer findHeight(JsonNode root) {
        for (JsonNode stream : root.path("streams")) {
            if ("video".equals(stream.path("codec_type").asString())) {
                int h = stream.path("height").asInt(0);
                return h > 0 ? h : null;
            }
        }
        return null;
    }

    private String findCodec(JsonNode root, String codecType) {
        for (JsonNode stream : root.path("streams")) {
            if (codecType.equals(stream.path("codec_type").asString())) {
                return stream.path("codec_name").asString(null);
            }
        }
        return null;
    }

    private HlsSession startSession(Path sourceFile, Long mediaId, long startSeconds, Integer resHeight) throws IOException {
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

        boolean useVaapi = "vaapi".equalsIgnoreCase(hwAccel);
        if (useVaapi) {
            command.add("-vaapi_device");
            command.add(vaapiDevice);
        }

        command.add("-i");
        command.add(sourceFile.toString());
        command.addAll(List.of("-map", "0:v:0", "-map", "0:a:0"));

        boolean needsDownscale = resHeight != null && resHeight > 1080;

        if (useVaapi) {
            command.add("-vf");
            command.add(needsDownscale ? "format=nv12,hwupload,scale_vaapi=w=-2:h=1080" : "format=nv12,hwupload");
            command.addAll(List.of("-c:v", "h264_vaapi"));
        } else {
            if (needsDownscale) {
                command.addAll(List.of("-vf", "scale=-2:1080"));
            }
            command.addAll(List.of("-c:v", "libx264", "-preset", "ultrafast"));
        }

        command.addAll(List.of(
                "-c:a", "aac", "-ac", "2",
                "-fps_mode", "cfr", "-avoid_negative_ts", "make_zero",
                "-sc_threshold", "0",
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