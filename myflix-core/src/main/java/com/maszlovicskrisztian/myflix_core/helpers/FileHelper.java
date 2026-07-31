package com.maszlovicskrisztian.myflix_core.helpers;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Set;

@Component
public class FileHelper {

    private static final Set<String> extensions = Set.of("mp4", "mkv", "avi", "webm", "mov");

    public boolean isSample(Path p) {
        if (p == null)
            return false;

        return p.toString().toLowerCase().contains("sample");
    }

    public boolean hasVideoExtension(Path p) {
        if (p == null)
            return false;

        return extensions.contains(getFileExtension(p.toString()).toLowerCase());
    }

    public String getFileExtension(String filename) {
        if (filename == null)
            return null;

        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}
