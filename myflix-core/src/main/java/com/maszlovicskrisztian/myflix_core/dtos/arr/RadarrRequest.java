package com.maszlovicskrisztian.myflix_core.dtos.arr;

public record RadarrRequest(
        int tmdbId,
        String title,
        int qualityProfileId,
        String rootFolderPath,
        boolean monitored,
        String minimumAvailability,
        AddOptions addOptions) {
}
