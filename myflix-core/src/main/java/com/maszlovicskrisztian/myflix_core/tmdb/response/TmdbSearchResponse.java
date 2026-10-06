package com.maszlovicskrisztian.myflix_core.tmdb.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSearchResponse(
        List<TmdbSearchResult> results,
        @JsonProperty("page") Integer currentPage,
        @JsonProperty("total_pages") Integer pages
) {}
