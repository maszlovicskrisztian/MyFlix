package com.maszlovicskrisztian.myflix_core.dtos.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MovieDetailsResponse(
        Long id,
        LocalDateTime addedAt,
        String overview,
        String title,
        String posterPath,
        String backdropPath,
        LocalDate releaseDate,
        Integer runtimeMinutes
) {
}
