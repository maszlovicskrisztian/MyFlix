package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;

public record TranslatedMovieResult(MovieMetadata movie, String localizedTitle, String localizedOverview) {
}
