package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.helpers.FileHelper;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
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

    public List<FileInfo> scanAndSave() throws IOException {
        List<Path> files = scanNewFiles();
        return mediaItemService.addMediaItems(files);
    }

    public List<Path> scanNewFiles() throws IOException {
        Set<String> existingItemPaths = mediaItemService.getAllRelativePaths();
        return scanAllFiles().stream().filter(p -> !existingItemPaths.contains(p.toString())).toList();
    }

    public List<Path> scanAllFiles() throws IOException {
        Path root = mediaPathResolver.getMediaPath();

        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(fileHelper::hasVideoExtension)
                    .filter(p -> !fileHelper.isSample(p))
                    .map(root::relativize)
                    .toList();
        }
    }
}
