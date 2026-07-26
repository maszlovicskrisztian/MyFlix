package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.WatchProgress;

public record WatchProgressDto(Long mediaId, Long profileId, Long progressSeconds) {
    public static WatchProgressDto from(WatchProgress model) {
        return new WatchProgressDto(
                model.getMediaItem().getId(),
                model.getProfile().getId(),
                model.getProgressSeconds()
        );
    }
}
