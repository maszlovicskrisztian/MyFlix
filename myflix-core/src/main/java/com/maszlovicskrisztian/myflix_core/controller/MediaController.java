package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.dtos.UpdateProgressRequest;
import com.maszlovicskrisztian.myflix_core.dtos.WatchProgressDto;
import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.service.MediaItemService;
import com.maszlovicskrisztian.myflix_core.service.WatchProgressService;
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

    private final MediaItemService mediaItemService;
    private final WatchProgressService watchProgressService;

    @GetMapping
    public List<MediaItemDto> getAllMedia() {
        return mediaItemService.getAllMedia();
    }

    @GetMapping("/{id}")
    public MediaItemDto getMediaById(@PathVariable Long id) {
        MediaItemDto dto = mediaItemService.getMediaDtoById(id);

        if (dto == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return dto;
    }

    @GetMapping("/movies")
    public List<MediaItemDto> getMovies() {
        return mediaItemService.getAllMovies();
    }

    @GetMapping("/{id}/progress")
    public WatchProgressDto getProgressForMediaByProfile(
            @PathVariable Long id,
            @RequestParam Long profileId) {

        WatchProgressDto dto = watchProgressService.getProgressForMediaByProfile(id, profileId);

        if (dto == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        return dto;
    }

    @PutMapping("/{id}/progress")
    public WatchProgressDto setProgressForMediaByProfile(
            @PathVariable Long id,
            @RequestParam Long profileId,
            @RequestBody UpdateProgressRequest request) {

        return watchProgressService.setProgressForMediaByProfile(id, profileId, request.progressSeconds());
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<ResourceRegion> stream(
            @PathVariable Long id,
            @RequestHeader HttpHeaders header) throws IOException {

        MediaItem mediaItem = mediaItemService.getMediaById(id);
        if (mediaItem == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

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
