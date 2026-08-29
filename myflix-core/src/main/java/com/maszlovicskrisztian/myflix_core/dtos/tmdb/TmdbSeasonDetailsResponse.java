package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSeasonDetailsResponse(
        Long id,
        String overview,
        String name,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("air_date") String airDate
) {}
