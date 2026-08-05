package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.*;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.helpers.PlaybackCompatibility;
import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.service.FfProbeService;
import com.maszlovicskrisztian.myflix_core.service.MediaItemService;
import com.maszlovicskrisztian.myflix_core.service.WatchProgressService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaItemService mediaItemService;
    private final WatchProgressService watchProgressService;
    private final MediaPathResolver mediaPathResolver;
    private final FfProbeService probeService;

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

    @GetMapping("/continue-watching")
    public List<MediaItemDto> getContinueWatchingList(@RequestParam Long profileId) {
        return watchProgressService.getMediasInWatchByProfile(profileId);
    }

    @GetMapping("/{id}/progress")
    public WatchProgressDto getProgressForMediaByProfile(
            @PathVariable Long id,
            @RequestParam Long profileId) {

        return watchProgressService.getProgressForMediaByProfile(id, profileId);
    }

    @PutMapping("/{id}/progress")
    public WatchProgressDto setProgressForMediaByProfile(
            @PathVariable Long id,
            @RequestParam Long profileId,
            @RequestBody UpdateProgressRequest request) {

        return watchProgressService.setProgressForMediaByProfile(id, profileId, request.progressSeconds());
    }

    @GetMapping("/{id}/stream")
    public void stream(@PathVariable Long id,
                       @RequestHeader HttpHeaders headers,
                       HttpServletResponse response) throws IOException {

        String relativePath = mediaItemService.getRelativePathById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        File file = Paths.get(mediaPathResolver.getMediaPath()).resolve(relativePath).toFile();
        long contentLength = file.length();
        List<HttpRange> ranges = headers.getRange();

        MediaType mediaType = MediaTypeFactory.getMediaType(new FileSystemResource(file))
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        response.setContentType(mediaType.toString());

        if (ranges.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentLengthLong(contentLength);
            try (InputStream in = new FileInputStream(file); OutputStream out = response.getOutputStream()) {
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

        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             OutputStream out = response.getOutputStream()) {
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

    @GetMapping("/{id}/playback-info")
    public PlaybackInfoDto getPlaybackInfo(
            @PathVariable Long id,
            @RequestParam Long profileId,
            @RequestParam(defaultValue = "false") boolean supportsMkv) {

        MediaItem item = mediaItemService.getMediaById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (item.getCodec() == null) probeAndPersist(item);

        MediaProbeResult probeResult = new MediaProbeResult(item.getCodec(), item.getAudioCodec(), item.getContainer(), item.getDurationSeconds());
        WatchProgressDto progress = watchProgressService.getProgressForMediaByProfile(id, profileId);
        long resumeSeconds = progress != null ? progress.progressSeconds() : 0L;

        return PlaybackCompatibility.isDirectPlayCompatible(probeResult, item.getRelativePath(), supportsMkv)
                ? new PlaybackInfoDto("DIRECT", "/media/" + id + "/stream", resumeSeconds, item.getDurationSeconds())
                : new PlaybackInfoDto("HLS", "/media/" + id + "/hls/playlist.m3u8", resumeSeconds, item.getDurationSeconds());
    }

    private void probeAndPersist(MediaItem item) {
        try {
            Path file = Paths.get(mediaPathResolver.getMediaPath()).resolve(item.getRelativePath());
            MediaProbeResult result = probeService.probe(file);
            item.setCodec(result.videoCodec());
            item.setAudioCodec(result.audioCodec());
            item.setContainer(result.container());
            item.setDurationSeconds(result.durationSeconds());
            mediaItemService.saveMedia(item);
        } catch (IOException e) {
            System.out.println("HIBA " + e.toString());
        }
    }
}
