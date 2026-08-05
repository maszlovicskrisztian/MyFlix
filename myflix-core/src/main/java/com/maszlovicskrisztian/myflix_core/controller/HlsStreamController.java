package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.service.HlsTranscodeService;
import com.maszlovicskrisztian.myflix_core.service.MediaItemService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media/{id}/hls")
public class HlsStreamController {
    private final HlsTranscodeService hlsTranscodeService;
    private final MediaItemService mediaItemService;
    private final MediaPathResolver mediaPathResolver;

    @GetMapping("/playlist.m3u8")
    public ResponseEntity<String> getPlaylist(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") long startSeconds,
            HttpServletRequest request) throws IOException {
        String relativePath = mediaItemService.getRelativePathById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Path sourceFile = Paths.get(mediaPathResolver.getMediaPath()).resolve(relativePath);
        Path playlist = hlsTranscodeService.getOrStartSession(sourceFile, id, startSeconds);

        try {
            hlsTranscodeService.waitForFirstSegment(playlist, Duration.ofSeconds(15), 3);
        } catch (TimeoutException e) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, e.getMessage());
        }

        String token = extractToken(request);
        String rewritten = rewriteSegmentUrls(playlist, id, token);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/vnd.apple.mpegurl"))
                .body(rewritten);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return request.getParameter("token");
    }

    private String rewriteSegmentUrls(Path playlist, Long mediaId, String token) throws IOException {
        StringBuilder result = new StringBuilder();
        for (String line : Files.readAllLines(playlist)) {
            if (line.endsWith(".ts")) {
                result.append("/api/media/").append(mediaId).append("/hls/").append(line)
                        .append("?token=").append(token);
            } else {
                result.append(line);
            }
            result.append("\n");
        }
        return result.toString();
    }

    @GetMapping("/{segmentName}")
    public ResponseEntity<Resource> getSegment(@PathVariable Long id, @PathVariable String segmentName) {

        if (!segmentName.matches("playlist\\d+\\.ts")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        hlsTranscodeService.touch(id);

        Path segmentPath = hlsTranscodeService.getSessionDir(id).resolve(segmentName);
        if (!Files.exists(segmentPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp2t"))
                .body(new FileSystemResource(segmentPath));
    }
}
