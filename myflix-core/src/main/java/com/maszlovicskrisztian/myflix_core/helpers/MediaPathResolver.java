package com.maszlovicskrisztian.myflix_core.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class MediaPathResolver {

    @Value("${MEDIA_PATH}")
    private String mediaPathString;

    public Path getMediaPath() {
        return Paths.get(mediaPathString);
    }
}
