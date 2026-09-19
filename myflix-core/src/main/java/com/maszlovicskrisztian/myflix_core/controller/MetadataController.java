package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.request.EnrichRequest;
import com.maszlovicskrisztian.myflix_core.dtos.request.MetadataUpdateRequest;
import com.maszlovicskrisztian.myflix_core.dtos.response.MetadataDetailsResponse;
import com.maszlovicskrisztian.myflix_core.service.MediaMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private final MediaMetadataService metadataService;

    @GetMapping("/{mediaId}")
    public MetadataDetailsResponse getMetadataForMedia(@PathVariable Long mediaId) {
        return metadataService.getMetadata(mediaId);
    }

    @PostMapping("/enrich")
    public void refreshMissingMetadata() {
        metadataService.enrich();
    }

    @PutMapping("/enrich/{mediaId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Long refreshMetadataForMediaByImdb(@PathVariable Long mediaId, @RequestBody EnrichRequest request) {
        metadataService.enrichMediaByImdbId(mediaId, request.imdbId());
        return mediaId;
    }

    @PutMapping("/update/{mediaId}")
    public void updateMetadataForMedia(@PathVariable Long mediaId, @RequestBody MetadataUpdateRequest request) {
        metadataService.updateMetadata(mediaId, request);
    }
}
