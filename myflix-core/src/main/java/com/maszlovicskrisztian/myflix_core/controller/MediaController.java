package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.*;
import com.maszlovicskrisztian.myflix_core.dtos.request.UpdateProgressRequest;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.PlaybackInfoResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.WatchProgressResponse;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.helpers.PlaybackCompatibility;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.service.MediaItemService;
import com.maszlovicskrisztian.myflix_core.service.TranscodeService;
import com.maszlovicskrisztian.myflix_core.service.WatchProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaItemService mediaItemService;
    private final WatchProgressService watchProgressService;
    private final MediaPathResolver mediaPathResolver;
    private final TranscodeService transcodeService;

    @GetMapping("/unknown")
    public List<MediaBaseResponse> getAllUnknownMedia() {
        return mediaItemService.getUnknownMedia();
    }

    @GetMapping("/continue-watching")
    public List<MediaBaseResponse> getContinueWatchingList(@RequestParam Long profileId) {
        return watchProgressService.getMediasInWatchByProfile(profileId);
    }

    @GetMapping("/{id}/progress")
    public WatchProgressResponse getProgressForMediaByProfile(
            @PathVariable Long id,
            @RequestParam Long profileId) {

        return watchProgressService.getProgressForMediaByProfile(id, profileId);
    }

    @PutMapping("/{id}/progress")
    public WatchProgressResponse setProgressForMediaByProfile(
            @PathVariable Long id,
            @RequestParam Long profileId,
            @RequestBody UpdateProgressRequest request) {

        return watchProgressService.setProgressForMediaByProfile(id, profileId, request.progressSeconds());
    }

    @GetMapping("/{id}/playback-info")
    public PlaybackInfoResponse getPlaybackInfo(
            @PathVariable Long id,
            @RequestParam Long profileId,
            @RequestParam(defaultValue = "false") boolean supportsMkv) throws IOException {

        FileInfo item = mediaItemService.getMediaById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        MediaProbeResult probeResult;
        if (item.getCodec() == null) {
            Path file = mediaPathResolver.getMediaPath().resolve(item.getRelativePath());
            probeResult = transcodeService.probe(file);
            item.setCodec(probeResult.videoCodec());
            item.setAudioCodec(probeResult.audioCodec());
            item.setContainer(probeResult.container());
            item.setDurationSeconds(probeResult.durationSeconds());
            item.setResHeight(probeResult.resHeight());
            mediaItemService.saveMedia(item);
        } else {
            probeResult = new MediaProbeResult(item.getCodec(), item.getAudioCodec(), item.getContainer(), item.getDurationSeconds(), item.getResHeight());
        }

        long resumeSeconds = watchProgressService.getProgressSecondsForMediaByProfile(id, profileId);

        return PlaybackCompatibility.isDirectPlayCompatible(probeResult, item.getRelativePath(), supportsMkv)
                ? new PlaybackInfoResponse("DIRECT", "/media/" + id + "/stream", resumeSeconds, item.getDurationSeconds())
                : new PlaybackInfoResponse("HLS", "/media/" + id + "/stream/playlist.m3u8", resumeSeconds, item.getDurationSeconds());
    }
}
