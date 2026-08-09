package com.maszlovicskrisztian.myflix_core.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MediaPathResolver {

    @Value("${MEDIA_PATH}")
    private String mediaPathString;

    @Value("${MEDIA_INCLUDE_FOLDERS}")
    private String includeFoldersRaw;

    public Path getMediaPath() {
        return Paths.get(mediaPathString);
    }

    public Set<String> getIncludeFolders() {
        if (includeFoldersRaw == null || includeFoldersRaw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(includeFoldersRaw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
