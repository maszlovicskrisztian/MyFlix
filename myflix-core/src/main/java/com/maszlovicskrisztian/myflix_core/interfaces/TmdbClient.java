package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;

import java.util.Optional;

public interface TmdbClient {
    Optional<TmdbSearchResult> searchBestMatch(String query, Integer year);
    TmdbDetailsResponse getMovieDetails(Long tmdbId);
    TmdbDetailsResponse getTvDetails(Long tmdbId);
}
