package com.maszlovicskrisztian.myflix_core.dtos;

import java.util.List;

public record SeasonDetails(
        String title,
        String overview,
        String posterPath,
        Integer seasonNumber,
        List<EpisodeDetails> episodes) {
}
