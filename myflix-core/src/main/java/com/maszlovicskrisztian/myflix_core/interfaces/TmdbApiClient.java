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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmdbApiClient implements TmdbClient{

    private final RestClient client;

    @Override
    public Optional<TmdbSearchResult> searchBestMatch(String query, String year) {
        TmdbSearchResponse response = client.get()
                .uri(uriBuilder -> uriBuilder.path("/search/multi")
                        .queryParam("query", query)
                        .build())
                .retrieve()
                .body(TmdbSearchResponse.class);

        if (response == null)
            return Optional.empty();

        MediaType[] mediaTypes = Arrays.stream(MediaType.values())
                .filter(x -> x != MediaType.TV_EPISODE)
                .toArray(MediaType[]::new);

        return response.results().stream()
                .filter(x -> Arrays.stream(mediaTypes).anyMatch(t -> t.name().toLowerCase().equals(x.mediaType())))
                .filter(x -> year == null || (x.releaseDate() != null && x.releaseDate().startsWith(year)))
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

    @Override
    public TmdbDetailsResponse getTvSeasonDetails(Long tvId, Integer season) {
        return client.get().uri("/tv/{id}/season/{season}", tvId, season).retrieve().body(TmdbDetailsResponse.class);
    }

    @Override
    public TmdbDetailsResponse getTvEpisodeDetails(Long tvId, Integer season, Integer episode) {
        return client.get().uri("/tv/{id}/season/{season}/episode/{episode}", tvId, season, episode).retrieve().body(TmdbDetailsResponse.class);
    }
}
