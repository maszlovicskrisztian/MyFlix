package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSearchResult(
        Long id,
        @JsonProperty("show_id") Long seriesId,
        @JsonProperty("media_type") String mediaType
) {}
