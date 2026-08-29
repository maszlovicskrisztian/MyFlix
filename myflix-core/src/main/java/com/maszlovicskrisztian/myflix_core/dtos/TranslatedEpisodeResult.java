package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;

public record TranslatedEpisodeResult(EpisodeMetadata episode, String localizedTitle, String localizedOverview) {
}
