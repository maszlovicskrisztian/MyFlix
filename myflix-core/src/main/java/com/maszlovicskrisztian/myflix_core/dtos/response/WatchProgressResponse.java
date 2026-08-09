package com.maszlovicskrisztian.myflix_core.dtos.response;

import com.maszlovicskrisztian.myflix_core.model.WatchProgress;

public record WatchProgressResponse(Long mediaId, Long profileId, Long progressSeconds) {
    public static WatchProgressResponse from(WatchProgress model) {
        return new WatchProgressResponse(
                model.getFileInfo().getId(),
                model.getProfile().getId(),
                model.getProgressSeconds()
        );
    }
}
