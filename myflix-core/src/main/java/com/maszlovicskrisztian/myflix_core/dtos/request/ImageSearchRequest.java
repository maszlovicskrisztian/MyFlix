package com.maszlovicskrisztian.myflix_core.dtos.request;

import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;

public record ImageSearchRequest(
        MediaType mediaType,
        Long tmdbId,
        String title,
        Integer season,
        Integer episode
) {
}
