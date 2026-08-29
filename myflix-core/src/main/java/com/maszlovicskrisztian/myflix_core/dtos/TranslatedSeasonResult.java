package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.SeasonMetadata;

import java.util.List;

public record TranslatedSeasonResult(SeasonMetadata season, String localizedTitle, String localizedOverview, List<TranslatedEpisodeResult> episodes) {
}
