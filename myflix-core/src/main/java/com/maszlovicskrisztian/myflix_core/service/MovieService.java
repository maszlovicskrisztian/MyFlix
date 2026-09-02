package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchRequest;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.mapping.MovieMapper;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.MovieMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final TranslationService translationService;
    private final TmdbClient tmdbClient;
    private final MovieMetadataRepository movieMetadataRepository;
    private final FileInfoRepository fileInfoRepository;
    private final MovieMapper movieMapper;

    public List<MediaBaseResponse> getAllMovies(String languageCode) {
        List<MovieMetadata> movies = movieMetadataRepository.findAll();

        if (!languageCode.equalsIgnoreCase("en")) {
            return translationService.translateMovies(movies, languageCode)
                    .stream().map(movieMapper::toMediaBaseResponse)
                    .sorted(Comparator.comparing(MediaBaseResponse::title))
                    .toList();
        }

        return movies
                .stream().map(movieMapper::toMediaBaseResponse)
                .sorted(Comparator.comparing(MediaBaseResponse::title))
                .toList();
    }

    public MovieDetailsResponse getMovieByFileInfoId(Long fileInfoId, String languageCode) {
        FileInfo movie = fileInfoRepository.findById(fileInfoId)
                .filter(x -> x.getMovieMetadata() != null)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find media by file info id: " + fileInfoId));

        if (!languageCode.equalsIgnoreCase("en")) {
            return movieMapper.toMovieDetails(translationService.translateMovie(movie.getMovieMetadata(), languageCode));
        }

        return movieMapper.toMovieDetails(movie);
    }

    public List<MediaSearchResponse> findAllTitleWithIdByQuery(String query, String languageCode) {
        if (languageCode.equalsIgnoreCase("en")) {
            return movieMetadataRepository
                    .findAll()
                    .stream().filter(x -> x.getTitle().contains(query))
                    .map(movieMapper::toMediaSearchResponse)
                    .toList();
        }

        TmdbSearchRequest request = new TmdbSearchRequest(query, null, null, null, languageCode);
        Set<Long> tmdbIds = tmdbClient.searchMovie(request).stream().map(TmdbSearchResult::id).collect(Collectors.toSet());
        List<MovieMetadata> movies = movieMetadataRepository
                .findAll()
                .stream().filter(x -> tmdbIds.contains(x.getTmdbId()))
                .toList();

        return translationService.translateMovies(movies, languageCode).stream().map(movieMapper::toMediaSearchResponse).toList();
    }
}
