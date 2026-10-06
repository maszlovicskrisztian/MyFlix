package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.tmdb.response.TmdbMovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.tmdb.TmdbClient;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
import com.maszlovicskrisztian.myflix_core.mapping.MovieMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscoverService {
    private final TmdbClient tmdbClient;
    private final MediaBaseMapper mediaBaseMapper;
    private final MovieMapper movieMapper;

    public List<MediaBaseResponse> discoverShows(int monthsBack, String languageCode) {
        return tmdbClient.discoverNewShows(monthsBack, languageCode)
                .stream().map(mediaBaseMapper::fromDiscover)
                .toList();
    }

    public List<MediaBaseResponse> discoverMovies(int monthsBack, String languageCode) {
        return tmdbClient.discoverNewMovies(monthsBack, languageCode)
                .stream().map(mediaBaseMapper::fromDiscover)
                .toList();
    }

    public MovieDetailsResponse discoveredMovieByTmdbId(Long tmdbId, String languageCode) {
        TmdbMovieDetailsResponse tmdbResponse = tmdbClient.getMovieDetails(tmdbId, languageCode);
        return movieMapper.toMovieDetails(tmdbResponse);
    }
}
