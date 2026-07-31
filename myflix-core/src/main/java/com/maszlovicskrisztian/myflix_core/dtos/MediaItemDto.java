package com.maszlovicskrisztian.myflix_core.dtos;

import com.maszlovicskrisztian.myflix_core.model.MediaItem;

import java.time.LocalDateTime;

public record MediaItemDto(
        Long id,
        String fileName,
        Long durationSeconds,
        LocalDateTime addedAt,
        String container,
        String codec
) {
    public static MediaItemDto from(MediaItem model) {
        return new MediaItemDto(
                model.getId(),
                model.getFileName(),
                model.getDurationSeconds(),
                model.getAddedAt(),
                model.getContainer(),
                model.getCodec()
        );
    }
}
