package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.mapping.MediaItemMapper;
import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.model.MediaType;
import com.maszlovicskrisztian.myflix_core.repository.MediaItemRepository;
import com.maszlovicskrisztian.myflix_core.repository.RelativePathProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
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
    private final MediaItemRepository mediaItemRepository;
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

        MediaItem item = new MediaItem();
        item.setRelativePath(relativePath);
        item.setFileName(String.valueOf(path.getFileName()));
        item.setSizeBytes(Files.size(path));
        item.setAddedAt(LocalDateTime.now());

        if (probeResult != null) {
            item.setCodec(probeResult.videoCodec());
            item.setAudioCodec(probeResult.audioCodec());
            item.setContainer(probeResult.container());
            item.setDurationSeconds(probeResult.durationSeconds());
        }

        mediaItemRepository.save(item);
    }

    public void saveMedia(MediaItem mediaItem) {
        if (mediaItem == null)
            return;

        mediaItemRepository.save(mediaItem);
    }

    public Set<String> getAllRelativePaths() {
        return mediaItemRepository.findAll().stream().map(MediaItem::getRelativePath).collect(Collectors.toSet());
    }

    public Optional<String> getRelativePathById(Long id) {
        Optional<RelativePathProjection> projection = mediaItemRepository.findById(id, RelativePathProjection.class);
        return projection.map(RelativePathProjection::getRelativePath);
    }

    public List<MediaItemDto> getAllMedia() {
        return mediaItemRepository.findAll().stream().map(mapper::from).toList();
    }

    public List<MediaItemDto> getAllMovies() {
        return mediaItemRepository
                .findAll()
                .stream().filter(x -> x.getMetadata() != null && x.getMetadata().getMediaType() == MediaType.MOVIE)
                .map(mapper::from)
                .toList();
    }

    public MediaItemDto getMediaDtoById(Long id) {
        return mediaItemRepository
                .findById(id)
                .map(mapper::from)
                .orElse(null);
    }

    public Optional<MediaItem> getMediaById(Long id) {
        return mediaItemRepository
                .findById(id);
    }
}
