package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
import com.maszlovicskrisztian.myflix_core.mapping.MediaSearchMapper;
import com.maszlovicskrisztian.myflix_core.mapping.MovieMapper;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.MovieMetadataRepository;
import com.maszlovicskrisztian.myflix_core.repository.projection.TitleProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieMetadataRepository movieMetadataRepository;
    private final FileInfoRepository fileInfoRepository;
    private final MovieMapper movieMapper;
    private final MediaSearchMapper searchMapper;
    private final MediaBaseMapper mediaBaseMapper;

    public List<MediaBaseResponse> getAllMovies() {
        return fileInfoRepository
                .findAll().stream()
                .filter(x -> x.getMovieMetadata() != null)
                .map(mediaBaseMapper::fromMovie)
                .sorted(Comparator.comparing(MediaBaseResponse::title))
                .toList();
    }

    public MovieDetailsResponse getMovieByFileInfoId(Long fileInfoId) {
        return fileInfoRepository.findById(fileInfoId)
                .filter(x -> x.getMovieMetadata() != null)
                .map(movieMapper::toMovieDetails)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find media by file info id: " + fileInfoId));
    }

    public List<MediaSearchResponse> findAllTitleWithIdByQuery(String query) {
        return movieMetadataRepository.findAllBy(TitleProjection.class)
                .stream().filter(x -> x.getTitle().contains(query))
                .map(searchMapper::fromMovie)
                .toList();
    }
}
