package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.request.EnrichRequest;
import com.maszlovicskrisztian.myflix_core.service.MediaMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MediaMetadataService metadataService;

    @PostMapping("/enrich")
    public void refreshMissingMetadata() {
        metadataService.enrich();
    }

    @PostMapping("/enrich/{mediaId}")
    public void refreshMetadataForMedia(@PathVariable Long mediaId, @RequestBody EnrichRequest request) {
        metadataService.enrichMediaByImdbId(mediaId, request.imdbId());
    }
}
