package com.maszlovicskrisztian.myflix_core.dtos;

import java.util.List;

public record ShowDetails(
        Long id,
        String title,
        String overview,
        String posterPath,
        String backdropPath,
        Integer seasonCount,
        Integer episodeCount,
        List<SeasonDetails> seasons,
        List<String> genres
) {
}
