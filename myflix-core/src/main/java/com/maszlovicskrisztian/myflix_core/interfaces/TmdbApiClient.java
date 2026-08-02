package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.ImdbSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;
import com.maszlovicskrisztian.myflix_core.model.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TmdbApiClient implements TmdbClient{

    private final RestClient client;

    @Override
    public Optional<TmdbSearchResult> searchBestMatch(String query, Integer year) {
        TmdbSearchResponse response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/search/multi")
                        .queryParam("query", query)
                        .queryParamIfPresent("year", Optional.ofNullable(year))
                        .build())
                .retrieve()
                .body(TmdbSearchResponse.class);

        if (response == null)
            return Optional.empty();

        MediaType[] mediaTypes = MediaType.values();

        return response.results().stream()
                .filter(x -> Arrays.stream(mediaTypes).anyMatch(t -> t.name().toLowerCase().equals(x.mediaType())))
                .findFirst();
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
}
