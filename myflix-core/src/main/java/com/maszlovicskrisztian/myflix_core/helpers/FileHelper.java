package com.maszlovicskrisztian.myflix_core.helpers;

import java.nio.file.Path;
import java.util.Set;

public final class FileHelper {

    private static final Set<String> extensions = Set.of("mp4", "mkv", "avi", "webm", "mov");

    public static boolean isSample(Path p) {
        if (p == null)
            return false;

        return p.toString().toLowerCase().contains("sample");
    }

    public static boolean hasVideoExtension(Path p) {
        if (p == null)
            return false;

        return extensions.contains(getFileExtension(p.toString()).toLowerCase());
    }

    public static String stripExtension(String filename) {
        if (filename == null)
            return null;

        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex >= 0)
            return filename.substring(0, lastDotIndex);

        return filename;
    }

    public static String getFileExtension(String filename) {
        if (filename == null)
            return null;

        int dotIndex = filename.lastIndexOf(".");
        if (dotIndex >= 0) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }

    public static String topLevelFolder(Path relative) {
        return relative.getNameCount() > 0 ? relative.getName(0).toString() : "";
    }
}
