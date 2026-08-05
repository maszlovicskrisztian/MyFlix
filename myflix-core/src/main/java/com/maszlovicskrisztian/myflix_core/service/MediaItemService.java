package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.mapping.MediaItemMapper;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MediaType;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.RelativePathProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaItemService {
    private final FileInfoRepository fileInfoRepository;
    private final MediaItemMapper mapper;
    private final MediaPathResolver mediaPathResolver;
    private final FfProbeService probeService;

    public void addMediaItems(List<Path> paths) {
        if (paths == null || paths.isEmpty())
            return;

        paths.forEach(p-> {
            try {
                addNewMediaItem(p);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void addNewMediaItem(Path path) throws IOException {
        if (path == null)
            return;

        MediaProbeResult probeResult;
        try {
            probeResult = probeService.probe(path);
        } catch (IOException e) {
            System.out.println("HIBA " + e.toString());
            probeResult = null;
        }

        Path root = Paths.get(mediaPathResolver.getMediaPath());
        String relativePath = root.relativize(path).toString();

        FileInfo item = new FileInfo();
        item.setRelativePath(relativePath);
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
        return fileInfoRepository.findAll().stream().map(FileInfo::getRelativePath).collect(Collectors.toSet());
    }

    public Optional<String> getRelativePathById(Long id) {
        Optional<RelativePathProjection> projection = fileInfoRepository.findById(id, RelativePathProjection.class);
        return projection.map(RelativePathProjection::getRelativePath);
    }

    public List<MediaItemDto> getAllMedia() {
        return fileInfoRepository.findAll().stream().map(mapper::from).toList();
    }

    public List<MediaItemDto> getAllMovies() {
        return fileInfoRepository
                .findAll()
                .stream().filter(x -> x.getMovieMetadata() != null)
                .map(mapper::from)
                .toList();
    }

    public MediaItemDto getMediaDtoById(Long id) {
        return fileInfoRepository
                .findById(id)
                .map(mapper::from)
                .orElse(null);
    }

    public Optional<FileInfo> getMediaById(Long id) {
        return fileInfoRepository
                .findById(id);
    }
}
