package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbEpisodeDetailsResponse(
        Long id,
        String overview,
        String name,
        @JsonProperty("still_path") String stillPath,
        @JsonProperty("air_date") String airDate,
        Integer runtime,
        @JsonProperty("episode_number") Integer episodeNumber
) {}
