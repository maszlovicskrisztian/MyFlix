package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbDiscoverResult(
        int id,
        String title,
        String name,
        String overview,
        @JsonProperty("poster_path") String posterPath)
{}
