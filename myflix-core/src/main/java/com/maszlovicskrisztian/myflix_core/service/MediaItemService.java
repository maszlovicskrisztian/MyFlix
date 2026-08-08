package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.RelativePathProjection;
import lombok.RequiredArgsConstructor;
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
public class MediaItemService {
    private final FileInfoRepository fileInfoRepository;
    private final MediaBaseMapper mapper;
    private final MediaPathResolver mediaPathResolver;
    private final TranscodeService transcodeService;

    public void addMediaItems(List<Path> relativePaths) {
        if (relativePaths == null || relativePaths.isEmpty())
            return;

        relativePaths.forEach(this::addNewMediaItem);
    }

    public void addNewMediaItem(Path relativePath) {
        if (relativePath == null)
            return;

        Path absoluteFile = mediaPathResolver.getMediaPath().resolve(relativePath);
        MediaProbeResult probeResult;
        try {
            probeResult = transcodeService.probe(absoluteFile);
        } catch (IOException e) {
            System.out.println("HIBA " + e.toString());
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
        }

        fileInfoRepository.save(item);
    }

    public void saveMedia(FileInfo fileInfo) {
        if (fileInfo == null)
            return;

        fileInfoRepository.save(fileInfo);
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