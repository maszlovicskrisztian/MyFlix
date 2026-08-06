package com.maszlovicskrisztian.myflix_core.dtos;

import java.time.LocalDate;

public record EpisodeDetails(
        String title,
        String overview,
        String stillPath,
        LocalDate releaseDate,
        Integer runtimeMinutes,
        Integer episodeNumber,
        Long fileInfoId) {
}
