package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.*;
import com.maszlovicskrisztian.myflix_core.dtos.request.UpdateProgressRequest;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.PlaybackInfoResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.WatchProgressResponse;
import com.maszlovicskrisztian.myflix_core.helpers.PlaybackCompatibility;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/media")
public class MediaController {

    private final MediaItemService mediaItemService;
    private final WatchProgressService watchProgressService;
    private final ShowService showService;
    private final MovieService movieService;

    @GetMapping("/search")
    public List<MediaSearchResponse> searchMedia(@RequestParam String query, @RequestParam String languageCode) {
        List<MediaSearchResponse> results = new ArrayList<>(showService.findAllTitleWithIdByQuery(query, languageCode));
        results.addAll(movieService.findAllTitleWithIdByQuery(query, languageCode));

        return results;
    }

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
            @RequestParam(defaultValue = "false") boolean supportsMkv) {

        FileInfo item = mediaItemService.getMediaById(id);
        if (item.getCodec() == null) {
            item = mediaItemService.saveFileMetadata(item);
        }

        MediaProbeResult probeResult = new MediaProbeResult(item.getCodec(), item.getAudioCodec(), item.getContainer(), item.getDurationSeconds(), item.getResHeight());

        long resumeSeconds = watchProgressService.getProgressSecondsForMediaByProfile(id, profileId);

        return PlaybackCompatibility.isDirectPlayCompatible(probeResult, item.getRelativePath(), supportsMkv)
                ? new PlaybackInfoResponse("DIRECT", "/media/" + id + "/stream", resumeSeconds, item.getDurationSeconds())
                : new PlaybackInfoResponse("HLS", "/media/" + id + "/stream/playlist.m3u8", resumeSeconds, item.getDurationSeconds());
    }
}
