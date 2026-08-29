package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbShowDetailsResponse(
        Long id,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        List<TmdbGenre> genres,
        String name,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("number_of_episodes") Integer episodeCount,
        @JsonProperty("number_of_seasons") Integer seasonCount
) {}
