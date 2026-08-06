package com.maszlovicskrisztian.myflix_core.dtos.response;

import com.maszlovicskrisztian.myflix_core.dtos.SeasonDetails;

import java.util.List;

public record ShowDetailsResponse(
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
