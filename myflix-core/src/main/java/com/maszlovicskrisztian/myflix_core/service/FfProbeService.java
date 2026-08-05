package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class FfProbeService {

    @Value("${ffprobe.path:ffprobe}")
    private String ffprobePath;

    private final ObjectMapper objectMapper;

    public MediaProbeResult probe(Path file) throws IOException {
        List<String> command = List.of(
                ffprobePath, "-v", "quiet", "-print_format", "json",
                "-show_format", "-show_streams", file.toString()
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);

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
            throw new IOException("ffprobe megszakítva", e);
        }
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("ffprobe időtúllépés: " + file);
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
}