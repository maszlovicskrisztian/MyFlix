package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.helpers.FileHelper;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
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

    private final MediaItemService mediaItemService;
    private final FileHelper fileHelper;
    private final MediaPathResolver mediaPathResolver;

    public void scan() throws IOException {
        String mediaPath = mediaPathResolver.getMediaPath();

        Path root = Paths.get(mediaPath);
        Set<String> existingItemPaths = mediaItemService.getAllRelativePaths();

        try (Stream<Path> paths = Files.walk(root)) {
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
