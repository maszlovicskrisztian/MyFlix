package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbDetailsResponse(
        //közös
        Long id,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        List<TmdbGenre> genres,
        Integer runtime,

        //film
        String title,
        @JsonProperty("release_date") String releaseDate,

        //sorozat
        String name,
        @JsonProperty("first_air_date") String firstAirDate,
        @JsonProperty("last_air_date") String LastAirDate,
        @JsonProperty("episode_run_time") List<Integer> episodeRunTime,

        //episode
        @JsonProperty("episode_number") Integer episodeNumber,
        @JsonProperty("season_number") Integer seasonNumber

) {}
