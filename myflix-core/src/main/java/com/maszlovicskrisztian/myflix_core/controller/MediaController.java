package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.repository.MediaItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Value("${MEDIA_PATH}")
    private String mediaPath;

    private final MediaItemRepository mediaItemRepository;

    @GetMapping
    public List<MediaItem> getAllMedia() {
        return mediaItemRepository.findAll();
    }

    @GetMapping("/{id}")
    public MediaItem getMediaById(@PathVariable Long id) {
        return mediaItemRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<ResourceRegion> stream(
            @PathVariable Long id,
            @RequestHeader HttpHeaders header) throws IOException {

        MediaItem mediaItem = mediaItemRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Path filePath = Paths.get(mediaPath).resolve(mediaItem.getRelativePath());
        FileSystemResource file = new FileSystemResource(filePath);

        long contentLength = file.contentLength();
        List<HttpRange> ranges = header.getRange();

        ResourceRegion region;

        if (ranges.isEmpty()) {
            long rangeLength = Math.min(1_000_000, contentLength);
            region = new ResourceRegion(file, 0, rangeLength);
        } else {
            HttpRange range = ranges.getFirst();
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1_000_000, end - start + 1);
            region = new ResourceRegion(file, start, rangeLength);
        }

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory.getMediaType(file).orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);

    }
}
