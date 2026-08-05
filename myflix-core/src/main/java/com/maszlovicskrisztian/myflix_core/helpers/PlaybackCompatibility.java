package com.maszlovicskrisztian.myflix_core.helpers;

import com.maszlovicskrisztian.myflix_core.dtos.MediaProbeResult;

import java.util.Set;

public class PlaybackCompatibility {

    private static final Set<String> COMPATIBLE_VIDEO_CODECS = Set.of("h264", "vp9", "av1");
    private static final Set<String> COMPATIBLE_AUDIO_CODECS = Set.of("aac", "opus", "vorbis", "mp3");
    private static final Set<String> COMPATIBLE_EXTENSIONS = Set.of("mp4", "m4v", "mov");

    public static boolean isDirectPlayCompatible(MediaProbeResult probeResult, String relativePath, boolean clientSupportsMkv) {
        boolean videoOk = probeResult.videoCodec() == null || COMPATIBLE_VIDEO_CODECS.contains(probeResult.videoCodec());
        boolean audioOk = probeResult.audioCodec() == null || COMPATIBLE_AUDIO_CODECS.contains(probeResult.audioCodec());
        boolean containerOk = isCompatibleExtension(relativePath)
                || (clientSupportsMkv && isMkvExtension(relativePath));
        return videoOk && audioOk && containerOk;
    }

    private static boolean isMkvExtension(String relativePath) {
        return relativePath.toLowerCase().endsWith(".mkv");
    }

    private static boolean isCompatibleExtension(String relativePath) {
        int dotIndex = relativePath.lastIndexOf('.');
        if (dotIndex == -1) return false;
        String extension = relativePath.substring(dotIndex + 1).toLowerCase();
        return COMPATIBLE_EXTENSIONS.contains(extension);
    }
}