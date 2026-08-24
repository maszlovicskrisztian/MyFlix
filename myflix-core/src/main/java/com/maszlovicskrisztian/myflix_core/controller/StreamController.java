package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.enums.ExceptionReason;
import com.maszlovicskrisztian.myflix_core.exception.MediaProcessingException;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.service.JwtService;
import com.maszlovicskrisztian.myflix_core.service.TranscodeService;
import com.maszlovicskrisztian.myflix_core.service.MediaItemService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media/{id}/stream")
public class StreamController {

    private final JwtService jwtService;
    private final TranscodeService transcodeService;
    private final MediaItemService mediaItemService;
    private final MediaPathResolver mediaPathResolver;

    @GetMapping()
    public void stream(@PathVariable Long id,
                       @RequestHeader HttpHeaders headers,
                       HttpServletResponse response) throws IOException {

        String relativePath = mediaItemService.getRelativePathById(id);
        File file = mediaPathResolver.getMediaPath().resolve(relativePath).toFile();
        long contentLength = file.length();
        List<HttpRange> ranges = headers.getRange();

        MediaType mediaType = MediaTypeFactory.getMediaType(new FileSystemResource(file))
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        response.setContentType(mediaType.toString());

        if (ranges.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLengthLong(contentLength);
            try (InputStream in = new FileInputStream(file); ServletOutputStream out = response.getOutputStream()) {
                in.transferTo(out);
            }
            return;
        }

        HttpRange range = ranges.getFirst();
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        long rangeLength = Math.min(1_000_000, end - start + 1);
        long rangeEnd = start + rangeLength - 1;

        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setContentLengthLong(rangeLength);
        response.setHeader(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + rangeEnd + "/" + contentLength);

        try (RandomAccessFile raf = new RandomAccessFile(file, "r"); ServletOutputStream out = response.getOutputStream()) {
            raf.seek(start);
            byte[] buffer = new byte[8192];
            long remaining = rangeLength;
            int read;
            while (remaining > 0 && (read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                out.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    @GetMapping("/playlist.m3u8")
    public ResponseEntity<String> getPlaylist(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") long startSeconds,
            HttpServletRequest request) {

        FileInfo media = mediaItemService.getMediaById(id);
        Path sourceFile = mediaPathResolver.getMediaPath().resolve(media.getRelativePath());
        Path playlist = transcodeService.getOrStartSession(sourceFile, id, startSeconds, media.getResHeight());
        transcodeService.waitForFirstSegment(playlist, Duration.ofSeconds(15), 3);

        String token = jwtService.extractToken(request);
        String rewritten = rewriteSegmentUrls(playlist, id, token);

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/vnd.apple.mpegurl"))
                .body(rewritten);
    }

    @GetMapping("/{segmentName}")
    public ResponseEntity<Resource> getSegment(@PathVariable Long id, @PathVariable String segmentName) {

        if (!segmentName.matches("playlist\\d+\\.ts")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        transcodeService.touch(id);

        Path segmentPath = transcodeService.getSessionDir(id).resolve(segmentName);
        if (!Files.exists(segmentPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/mp2t"))
                .body(new FileSystemResource(segmentPath));
    }

    private String rewriteSegmentUrls(Path playlist, Long mediaId, String token) {
        try {
            StringBuilder result = new StringBuilder();
            for (String line : Files.readAllLines(playlist)) {
                if (line.endsWith(".ts")) {
                    result.append("/api/media/").append(mediaId).append("/stream/").append(line)
                            .append("?token=").append(token);
                } else {
                    result.append(line);
                }
                result.append("\n");
            }
            return result.toString();
        } catch (IOException e) {
            throw new MediaProcessingException("Error during stream: Playlist file damaged or missing", ExceptionReason.IO_ERROR, e);
        }
    }
}
