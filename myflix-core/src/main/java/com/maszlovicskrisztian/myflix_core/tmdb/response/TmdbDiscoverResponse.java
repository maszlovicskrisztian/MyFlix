package com.maszlovicskrisztian.myflix_core.tmdb.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbDiscoverResponse(
        List<TmdbDiscoverResult> results,
        @JsonProperty("total_pages") Integer pages) {
}
