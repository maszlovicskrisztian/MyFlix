package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.*;
import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbApiClient implements TmdbClient{

    private final RestClient client;

    @Value("${tmdb.series.watch-providers}")
    private String seriesWatchProviders;

    @Value("${tmdb.region:HU}")
    private String region;

    @Override
    public TmdbSearchResult searchBestMatch(TmdbSearchRequest request) {
        if (request == null)
            return null;

        List<TmdbSearchResult> searchResults;
        MediaType mediaType;
        if (request.season() != null && request.episode() != null) {
            searchResults = searchTV(request);
            mediaType = MediaType.TV;
        }
        else {
            searchResults = searchMovie(request);
            mediaType = MediaType.MOVIE;
        }

        if (searchResults.size() > 1) {
            log.debug("TMDB search resulted more than one element, returning the closest match.");

            if (mediaType == MediaType.MOVIE)
                searchResults.sort((x1, x2) ->
                        getWinklerDistance(x1.movieTitle().toLowerCase(), request.title().toLowerCase()) -
                                getWinklerDistance(x2.movieTitle().toLowerCase(), request.title().toLowerCase()));
            else
                searchResults.sort((x1, x2) ->
                        getWinklerDistance(x1.showTitle().toLowerCase(), request.title().toLowerCase()) -
                                getWinklerDistance(x2.showTitle().toLowerCase(), request.title().toLowerCase()));
        }

        return searchResults.isEmpty() ? null : searchResults.getFirst();
    }

    private int getWinklerDistance(String foundTitle, String searchTitle) {
        JaroWinklerDistance winklerDistance = new JaroWinklerDistance();
        return ((int) (winklerDistance.apply(foundTitle, searchTitle) * 100));
    }

    @Override
    public List<TmdbSearchResult> searchMovie(TmdbSearchRequest request) {
        if (request == null || request.title() == null)
            return List.of();

        TmdbSearchResponse response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/search/movie")
                        .queryParam("query", request.title())
                        .queryParamIfPresent("primary_release_year", Optional.ofNullable(request.year()))
                        .queryParamIfPresent("language", Optional.ofNullable(request.languageCode()))
                        .build())
                .retrieve()
                .body(TmdbSearchResponse.class);

        if (response == null)
            return List.of();

        List<TmdbSearchResult> results = response.results();
        if (response.pages() > 1) {
            Integer currentPage = response.currentPage();
            int pageCount = response.pages() + 1;

            for (int i = currentPage + 1; i < pageCount; i++) {
                int finalI = i;
                response = client.get()
                        .uri(uriBuilder -> uriBuilder.path("/search/movie")
                                .queryParam("query", request.title())
                                .queryParamIfPresent("primary_release_year", Optional.ofNullable(request.year()))
                                .queryParamIfPresent("language", Optional.ofNullable(request.languageCode()))
                                .queryParam("page", finalI)
                                .build())
                        .retrieve()
                        .body(TmdbSearchResponse.class);

                if (response != null)
                    results.addAll(response.results());
            }
        }

        return results;
    }

    @Override
    public List<TmdbSearchResult> searchTV(TmdbSearchRequest request) {
        if (request == null || request.title() == null)
            return List.of();

        TmdbSearchResponse response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/search/tv")
                        .queryParam("query", request.title())
                        .queryParamIfPresent("year", Optional.ofNullable(request.year()))
                        .queryParamIfPresent("language", Optional.ofNullable(request.languageCode()))
                        .build())
                .retrieve()
                .body(TmdbSearchResponse.class);

        if (response == null)
            return List.of();

        List<TmdbSearchResult> results = response.results();
        if (response.pages() > 1) {
            Integer currentPage = response.currentPage();
            int pageCount = response.pages() + 1;

            for (int i = currentPage + 1; i < pageCount; i++) {
                int finalI = i;
                response = client.get()
                        .uri(uriBuilder -> uriBuilder.path("/search/tv")
                                .queryParam("query", request.title())
                                .queryParamIfPresent("year", Optional.ofNullable(request.year()))
                                .queryParamIfPresent("language", Optional.ofNullable(request.languageCode()))
                                .queryParam("page", finalI)
                                .build())
                        .retrieve()
                        .body(TmdbSearchResponse.class);

                if (response != null)
                    results.addAll(response.results());
            }
        }

        return results;
    }

    @Override
    public ImdbSearchResponse searchByImdbId(String imdbId) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/find/{external_id}")
                        .queryParam("external_source", "imdb_id")
                        .build(imdbId))
                .retrieve()
                .body(ImdbSearchResponse.class);
    }

    @Override
    public TmdbMovieDetailsResponse getMovieDetails(Long tmdbId, String language) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/{id}")
                        .queryParamIfPresent("language", Optional.ofNullable(language))
                        .build(tmdbId))
                .retrieve().body(TmdbMovieDetailsResponse.class);
    }

    @Override
    public TmdbShowDetailsResponse getTvDetails(Long tmdbId, String language) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/tv/{id}")
                        .queryParamIfPresent("language", Optional.ofNullable(language))
                        .build(tmdbId))
                .retrieve().body(TmdbShowDetailsResponse.class);
    }

    @Override
    public TmdbSeasonDetailsResponse getTvSeasonDetails(Long tvId, Integer season, String language) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/tv/{id}/season/{season}")
                        .queryParamIfPresent("language", Optional.ofNullable(language))
                        .build(tvId,season))
                .retrieve().body(TmdbSeasonDetailsResponse.class);
    }

    @Override
    public TmdbEpisodeDetailsResponse getTvEpisodeDetails(Long tvId, Integer season, Integer episode, String language) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/tv/{id}/season/{season}/episode/{episode}")
                        .queryParamIfPresent("language", Optional.ofNullable(language))
                        .build(tvId, season, episode))
                .retrieve().body(TmdbEpisodeDetailsResponse.class);
    }

    @Override
    public List<TmdbDiscoverResult> discoverNewMovies(int monthsBack) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(monthsBack);

        var response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/discover/movie")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("primary_release_date.gte", from)
                        .queryParam("primary_release_date.lte", to)
                        .build()
                )
                .retrieve().body(TmdbDiscoverResponse.class);

        return response == null ? List.of() : response.results();
    }

    @Override
    public List<TmdbDiscoverResult> discoverNewShows(int monthsBack) {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusMonths(monthsBack);

        var response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/discover/tv")
                        .queryParam("sort_by", "popularity.desc")
                        .queryParam("air_date.gte", from)
                        .queryParam("air_date.lte", to)
                        .queryParam("watch_region", region)
                        .queryParam("with_watch_providers", seriesWatchProviders)
                        .build()
                )
                .retrieve().body(TmdbDiscoverResponse.class);

        return response == null ? List.of() : response.results();
    }
}
