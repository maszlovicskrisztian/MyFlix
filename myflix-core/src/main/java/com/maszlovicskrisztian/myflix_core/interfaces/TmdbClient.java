package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.ImdbSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;

import java.util.Optional;

public interface TmdbClient {
    Optional<TmdbSearchResult> searchBestMatch(String query, Integer year);
    ImdbSearchResponse searchByImdbId(String imdbId);
    TmdbDetailsResponse getMovieDetails(Long tmdbId);
    TmdbDetailsResponse getTvDetails(Long tmdbId);
}
