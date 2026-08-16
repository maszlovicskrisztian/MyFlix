package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.projection.RelativePathProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
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

    public List<FileInfo> addMediaItems(List<Path> relativePaths) {
        if (relativePaths == null || relativePaths.isEmpty())
            return List.of();

        return relativePaths.stream().map(this::addNewMediaItem).toList();
    }

    public FileInfo addNewMediaItem(Path relativePath) {
        if (relativePath == null)
            return null;

        Path absoluteFile = mediaPathResolver.getMediaPath().resolve(relativePath);
        MediaProbeResult probeResult = transcodeService.probe(absoluteFile, false);

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
        return fileInfoRepository
                .findAllBy(RelativePathProjection.class)
                .stream().map(RelativePathProjection::getRelativePath)
                .collect(Collectors.toSet());
    }

    public String getRelativePathById(Long fileInfoId) {
        return fileInfoRepository
                .findById(fileInfoId, RelativePathProjection.class)
                .map(RelativePathProjection::getRelativePath)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find relative path by file info id: " + fileInfoId));
    }

    public List<MediaBaseResponse> getUnknownMedia() {
        return fileInfoRepository.findAll().stream()
                .filter(x -> x.getMovieMetadata() == null && x.getEpisodeMetadata() == null)
                .map(mapper::fromUnknownMedia)
                .toList();
    }

    public FileInfo getMediaById(Long fileInfoId) {
        return fileInfoRepository.findById(fileInfoId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find media by file info id: " + fileInfoId));
    }

    public List<FileInfo> getAll() {
        return fileInfoRepository
                .findAll();
    }

    public void deleteFileInfos(List<FileInfo> files) {
        fileInfoRepository.deleteAll(files);
    }

    public FileInfo saveFileMetadata(FileInfo item) {
        Path file = mediaPathResolver.getMediaPath().resolve(item.getRelativePath());
        MediaProbeResult probeResult = transcodeService.probe(file, true);
        item.setCodec(probeResult.videoCodec());
        item.setAudioCodec(probeResult.audioCodec());
        item.setContainer(probeResult.container());
        item.setDurationSeconds(probeResult.durationSeconds());
        item.setResHeight(probeResult.resHeight());

        return saveMedia(item);
    }
}