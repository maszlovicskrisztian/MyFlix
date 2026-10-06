package com.maszlovicskrisztian.myflix_core.tmdb.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.maszlovicskrisztian.myflix_core.tmdb.TmdbGenre;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovieDetailsResponse(
        Long id,
        String overview,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("backdrop_path") String backdropPath,
        List<TmdbGenre> genres,
        Integer runtime,
        String title,
        @JsonProperty("release_date") String releaseDate
)
{}
