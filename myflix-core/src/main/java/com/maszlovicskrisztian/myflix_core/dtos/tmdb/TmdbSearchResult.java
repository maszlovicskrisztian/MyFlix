package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbSearchResult(
        Long id,
        @JsonProperty("show_id") Long showId,
        @JsonProperty("episode_number") Integer episode,
        @JsonProperty("season_number") Integer season,
        @JsonProperty("name") String showTitle,
        @JsonProperty("title") String movieTitle
) {}
