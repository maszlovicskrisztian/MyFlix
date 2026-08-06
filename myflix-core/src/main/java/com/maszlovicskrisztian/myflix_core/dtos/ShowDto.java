package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.Show;

public record ShowDto(Long id, String title, String posterPath) {
    public static ShowDto from(Show model) {
        return new ShowDto(model.getId(), model.getTitle(), model.getPosterPath());
    }
}
