package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.Show;

public record TranslatedShowResult(Show show, String localizedTitle, String localizedOverview) {
}
