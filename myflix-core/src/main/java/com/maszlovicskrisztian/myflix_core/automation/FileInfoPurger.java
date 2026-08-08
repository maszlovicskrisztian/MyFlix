package com.maszlovicskrisztian.myflix_core.automation;

import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.service.LibraryScanner;
import com.maszlovicskrisztian.myflix_core.service.MediaItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FileInfoPurger {
    private final LibraryScanner libraryScanner;
    private final MediaItemService mediaItemService;

    @Scheduled(fixedDelay = 24, timeUnit = TimeUnit.HOURS)
    public void purgeFileInfoWithMissingFile() {
        try {
            Set<String> files = libraryScanner.scanAllFiles().stream().map(Path::toString).collect(Collectors.toSet());
            List<FileInfo> removedFiles = mediaItemService.getAll()
                    .stream().filter(f -> !files.contains(f.getRelativePath())).toList();

            mediaItemService.deleteFileInfos(removedFiles);
        } catch (Exception e) {
            //logoljuk majd
        }
    }
}
