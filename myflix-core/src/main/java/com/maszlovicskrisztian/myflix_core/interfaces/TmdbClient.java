package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.*;

import java.util.List;

public interface TmdbClient {
    TmdbSearchResult searchBestMatch(TmdbSearchRequest request);
    ImdbSearchResponse searchByImdbId(String imdbId);
    List<TmdbSearchResult> searchMovie(TmdbSearchRequest request);
    List<TmdbSearchResult> searchTV(TmdbSearchRequest request);
    TmdbMovieDetailsResponse getMovieDetails(Long tmdbId, String language);
    TmdbShowDetailsResponse getTvDetails(Long tmdbId, String language);
    TmdbSeasonDetailsResponse getTvSeasonDetails(Long tvId, Integer season, String language);
    TmdbEpisodeDetailsResponse getTvEpisodeDetails(Long tvId, Integer season, Integer episode, String language);
    List<TmdbDiscoverResult> discoverNewMovies(int monthsBack, String languageCode);
    List<TmdbDiscoverResult> discoverNewShows(int monthsBack, String languageCode);
}
