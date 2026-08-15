package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.*;
import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.apache.commons.text.similarity.JaroWinklerDistance;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TmdbApiClient implements TmdbClient{

    private final RestClient client;

    @Override
    public TmdbSearchResult searchBestMatch(TmdbSearchRequest request) {
        if (request == null)
            return null;

        List<TmdbSearchResult> searchResults;
        MediaType mediaType;
        if (request.season() != null && request.episode() != null) {
            searchResults = searchTV(request.title(), request.year());
            mediaType = MediaType.TV;
        }
        else {
            searchResults = searchMovie(request.title(), request.year());
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

    private List<TmdbSearchResult> searchMovie(String title, String year) {
        if (title == null)
            return List.of();

        TmdbSearchResponse response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/search/movie")
                        .queryParam("query", title)
                        .queryParamIfPresent("primary_release_year", Optional.ofNullable(year))
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
                                .queryParam("query", title)
                                .queryParamIfPresent("primary_release_year", Optional.ofNullable(year))
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

    private List<TmdbSearchResult> searchTV(String title, String year) {
        if (title == null)
            return List.of();

        TmdbSearchResponse response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/search/tv")
                        .queryParam("query", title)
                        .queryParamIfPresent("year", Optional.ofNullable(year))
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
                                .queryParam("query", title)
                                .queryParamIfPresent("year", Optional.ofNullable(year))
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
    public TmdbDetailsResponse getMovieDetails(Long tmdbId) {
        return client.get().uri("/movie/{id}", tmdbId).retrieve().body(TmdbDetailsResponse.class);
    }

    @Override
    public TmdbDetailsResponse getTvDetails(Long tmdbId) {
        return client.get().uri("/tv/{id}", tmdbId).retrieve().body(TmdbDetailsResponse.class);
    }

    @Override
    public TmdbDetailsResponse getTvSeasonDetails(Long tvId, Integer season) {
        return client.get().uri("/tv/{id}/season/{season}", tvId, season).retrieve().body(TmdbDetailsResponse.class);
    }

    @Override
    public TmdbDetailsResponse getTvEpisodeDetails(Long tvId, Integer season, Integer episode) {
        return client.get().uri("/tv/{id}/season/{season}/episode/{episode}", tvId, season, episode).retrieve().body(TmdbDetailsResponse.class);
    }
}
