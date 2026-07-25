package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.repository.MediaItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaItemService {
    private final MediaItemRepository mediaItemRepository;

    @Value("${MEDIA_PATH}")
    private String mediaPath;

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

        Path root = Paths.get(mediaPath);
        String relativePath = root.relativize(path).toString();

        MediaItem item = new MediaItem();
        item.setRelativePath(relativePath);
        item.setTitle(String.valueOf(path.getFileName()));
        item.setSizeBytes(Files.size(path));
        item.setAddedAt(LocalDateTime.now());
        mediaItemRepository.save(item);
    }

    public Set<String> getAllRelativePaths() {
        return mediaItemRepository.findAll().stream().map(MediaItem::getRelativePath).collect(Collectors.toSet());
    }

    public List<MediaItemDto> getAllMedia() {
        return mediaItemRepository.findAll().stream().map(MediaItemDto::from).toList();
    }

    public MediaItemDto getMediaDtoById(Long id) {
        return mediaItemRepository
                .findById(id)
                .map(MediaItemDto::from)
                .orElse(null);
    }

    public MediaItem getMediaById(Long id) {
        return mediaItemRepository
                .findById(id)
                .orElse(null);
    }
}
