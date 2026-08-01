package com.maszlovicskrisztian.myflix_core.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MediaItemDto(
        Long id,
        String fileName,
        LocalDateTime addedAt,
        String container,
        String codec,

        Long tmdbId,
        String overview,
        String title,
        String posterPath,
        String backdropPath,
        LocalDate releaseDate,
        Integer runtimeMinutes
) {}
