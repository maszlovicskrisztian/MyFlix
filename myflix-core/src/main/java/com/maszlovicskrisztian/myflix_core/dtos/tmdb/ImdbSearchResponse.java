package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ImdbSearchResponse(

    @JsonProperty("movie_results")
    List<TmdbSearchResult> movieResults,
    @JsonProperty("tv_results")
    List<TmdbSearchResult> tvResults,
    @JsonProperty("tv_episode_results")
    List<TmdbSearchResult> episodeResults
) {}
