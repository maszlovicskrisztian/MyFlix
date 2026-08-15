package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.helpers.FileHelper;
import com.maszlovicskrisztian.myflix_core.helpers.MediaPathResolver;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LibraryScanner {

    private final MediaItemService mediaItemService;
    private final FileHelper fileHelper;
    private final MediaPathResolver mediaPathResolver;
    private final MediaMetadataService mediaMetadataService;

    public void runScanAndEnrich() {
        List<FileInfo> newFiles = scanAndSave();
        newFiles.forEach(mediaMetadataService::enrichMedia);
    }

    public List<FileInfo> scanAndSave() {
        List<Path> files = scanNewFiles();
        return mediaItemService.addMediaItems(files);
    }

    public List<Path> scanNewFiles() {
        Set<String> existingItemPaths = mediaItemService.getAllRelativePaths();
        List<Path> newFiles = scanAllFiles()
                .stream().filter(p -> !existingItemPaths.contains(p.toString()))
                .toList();

        log.info("Scan found {} new files", newFiles.size());
        return newFiles;
    }

    public List<Path> scanAllFiles() {
        log.trace("File scan started.");
        Path root = mediaPathResolver.getMediaPath();
        Set<String> includeFolders = mediaPathResolver.getIncludeFolders();

        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> result = paths
                    .filter(Files::isRegularFile)
                    .filter(fileHelper::hasVideoExtension)
                    .filter(p -> !fileHelper.isSample(p))
                    .map(root::relativize)
                    .filter(p -> includeFolders.isEmpty() || includeFolders.contains(fileHelper.topLevelFolder(p)))
                    .toList();

            log.trace("File scan finished.");
            return result;
        } catch (IOException exception) {
            log.error("Error during library scan: {}", exception.getLocalizedMessage());
            return List.of();
        }
    }
}
