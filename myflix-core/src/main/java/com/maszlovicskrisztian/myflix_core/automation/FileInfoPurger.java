package com.maszlovicskrisztian.myflix_core.automation;

import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.service.LibraryScanner;
import com.maszlovicskrisztian.myflix_core.service.MediaItemService;
import com.maszlovicskrisztian.myflix_core.service.ShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileInfoPurger {
    private final LibraryScanner libraryScanner;
    private final MediaItemService mediaItemService;
    private final ShowService showService;

    @Scheduled(fixedDelayString = "${FILE_INFO_PURGE_INTERVAL_HOURS}", timeUnit = TimeUnit.HOURS)
    public void purgeFileInfoWithMissingFile() {
        try {
            log.trace("FileInfo purge started");

            Set<String> files = libraryScanner.scanAllFiles().stream().map(Path::toString).collect(Collectors.toSet());
            List<FileInfo> allFiles = mediaItemService.getAll();
            List<FileInfo> removedFiles = allFiles.stream().filter(f -> !files.contains(f.getRelativePath())).toList();

            log.info("Found {} file info to be removed", removedFiles.size());

            mediaItemService.deleteFileInfos(removedFiles);
            showService.deleteEmptyShows();

            log.trace("FileInfo purge finished");
        } catch (Exception e) {
            log.error("Error during file info purge: {}", e.getMessage());
        }
    }
}
