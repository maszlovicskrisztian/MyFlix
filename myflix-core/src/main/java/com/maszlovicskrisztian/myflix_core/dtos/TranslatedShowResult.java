package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.Show;

import java.util.List;

public record TranslatedShowResult(Show show, String localizedTitle, String localizedOverview, List<TranslatedSeasonResult> seasons) {
}
