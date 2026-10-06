package com.maszlovicskrisztian.myflix_core.tmdb.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.maszlovicskrisztian.myflix_core.tmdb.TmdbImage;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbImageResponse(
        List<TmdbImage> backdrops,
        List<TmdbImage> posters
) {
}
