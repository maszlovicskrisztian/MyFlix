package com.maszlovicskrisztian.myflix_core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class LibraryScanner {

    @Value("${MEDIA_PATH}")
    private String mediaPath;

    private final MediaItemService mediaItemService;
    private final FileHelper fileHelper;

    public void scan() throws IOException {
        Path root = Paths.get(mediaPath);
        Set<String> existingItemPaths = mediaItemService.getAllRelativePaths();

        try (Stream<Path> paths = Files.walk(Paths.get(mediaPath))) {
                List<Path> videoFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(fileHelper::hasVideoExtension)
                        .filter(p -> !fileHelper.isSample(p))
                        .filter(p -> !existingItemPaths.contains(root.relativize(p).toString()))
                        .toList();

                mediaItemService.addMediaItems(videoFiles);
        }
    }

}
