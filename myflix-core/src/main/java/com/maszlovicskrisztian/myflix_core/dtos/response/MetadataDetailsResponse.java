package com.maszlovicskrisztian.myflix_core.dtos.response;

import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;

import java.time.LocalDate;
import java.util.List;

public record MetadataDetailsResponse(
        String relativePath,
        Long mediaId,
        MediaType mediaType,
        Long tmdbId,
        String title,
        String overview,
        String backdropPath,
        String posterPath,
        LocalDate releaseDate,
        Integer runtimeMinutes,
        List<String> genres,
        Long showId,
        Integer seasonNumber,
        Integer episodeNumber) {
}
