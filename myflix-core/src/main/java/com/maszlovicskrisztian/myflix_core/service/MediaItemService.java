package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
import com.maszlovicskrisztian.myflix_core.mapping.MediaSearchMapper;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.MovieMetadataRepository;
import com.maszlovicskrisztian.myflix_core.repository.projection.RelativePathProjection;
import com.maszlovicskrisztian.myflix_core.repository.projection.TitleProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaItemService {
    private final FileInfoRepository fileInfoRepository;
    private final MediaBaseMapper mapper;
    private final MediaPathResolver mediaPathResolver;
    private final TranscodeService transcodeService;
    private final MovieMetadataRepository movieMetadataRepository;
    private final MediaSearchMapper searchMapper;

    public List<FileInfo> addMediaItems(List<Path> relativePaths) {
        if (relativePaths == null || relativePaths.isEmpty())
            return null;

        return relativePaths.stream().map(this::addNewMediaItem).toList();
    }

    public FileInfo addNewMediaItem(Path relativePath) {
        if (relativePath == null)
            return null;

        Path absoluteFile = mediaPathResolver.getMediaPath().resolve(relativePath);
        MediaProbeResult probeResult;
        try {
            probeResult = transcodeService.probe(absoluteFile);
        } catch (IOException e) {
            log.error("Error during ffprobe task: {}", e.getMessage());
            probeResult = null;
        }

        FileInfo item = new FileInfo();
        item.setRelativePath(relativePath.toString());
        item.setAddedAt(LocalDateTime.now());

        if (probeResult != null) {
            item.setCodec(probeResult.videoCodec());
            item.setAudioCodec(probeResult.audioCodec());
            item.setContainer(probeResult.container());
            item.setDurationSeconds(probeResult.durationSeconds());
            item.setResHeight(probeResult.resHeight());
        }

        return saveMedia(item);
    }

    public FileInfo saveMedia(FileInfo fileInfo) {
        if (fileInfo == null)
            return null;

        FileInfo saved = fileInfoRepository.save(fileInfo);
        log.info("Media from: {} saved successfully", fileInfo.getRelativePath());
        return saved;
    }

    public Set<String> getAllRelativePaths() {
        List<RelativePathProjection> projections = fileInfoRepository.findAllBy(RelativePathProjection.class);
        return projections.stream().map(RelativePathProjection::getRelativePath).collect(Collectors.toSet());
    }

    public Optional<String> getRelativePathById(Long id) {
        Optional<RelativePathProjection> projection = fileInfoRepository.findById(id, RelativePathProjection.class);
        return projection.map(RelativePathProjection::getRelativePath);
    }

    public List<MediaBaseResponse> getUnknownMedia() {
        return fileInfoRepository.findAll().stream()
                .filter(x -> x.getMovieMetadata() == null && x.getEpisodeMetadata() == null)
                .map(mapper::fromUnknownMedia)
                .toList();
    }

    public List<FileInfo> getAllMovies() {
        return fileInfoRepository
                .findAll()
                .stream().filter(x -> x.getMovieMetadata() != null)
                .toList();
    }

    public List<MediaSearchResponse> findAllTitleWithIdByQuery(String query) {
        return movieMetadataRepository.findAllBy(TitleProjection.class)
                .stream().filter(x -> x.getTitle().contains(query))
                .map(searchMapper::fromMovie)
                .toList();
    }

    public Optional<FileInfo> getMediaById(Long id) {
        return fileInfoRepository.findById(id);
    }

    public List<FileInfo> getAll() {
        return fileInfoRepository
                .findAll();
    }

    public void deleteFileInfos(List<FileInfo> files) {
        fileInfoRepository.deleteAll(files);
    }
}