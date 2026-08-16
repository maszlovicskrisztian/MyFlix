package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.ImdbSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchRequest;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;

import java.util.List;
import java.util.Optional;

public interface TmdbClient {
    TmdbSearchResult searchBestMatch(TmdbSearchRequest request);
    ImdbSearchResponse searchByImdbId(String imdbId);
    List<TmdbSearchResult> searchMovie(TmdbSearchRequest request);
    List<TmdbSearchResult> searchTV(TmdbSearchRequest request);
    TmdbDetailsResponse getMovieDetails(Long tmdbId, String language);
    TmdbDetailsResponse getTvDetails(Long tmdbId, String language);
    TmdbDetailsResponse getTvEpisodeDetails(Long tvId, Integer season, Integer episode, String language);
    TmdbDetailsResponse getTvSeasonDetails(Long tvId, Integer season, String language);
}
