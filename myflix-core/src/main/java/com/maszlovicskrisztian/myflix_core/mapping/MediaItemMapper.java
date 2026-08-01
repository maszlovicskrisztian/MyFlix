package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.model.MediaMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaItemMapper {
    @Value("${tmdb.images.base-url}")
    private String imagesBase;

    public MediaItemDto from(MediaItem model) {
        MediaMetadata metadata = model.getMetadata();

        if (metadata == null) {
            return new MediaItemDto(
                    model.getId(),
                    model.getFileName(),
                    model.getAddedAt(),
                    model.getContainer(),
                    model.getCodec(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        } else {
            String posterPath = imagesBase + "/w500" + metadata.getPosterPath();
            String backdropPath = imagesBase + "/w500" + metadata.getBackdropPath();

            return new MediaItemDto(
                    model.getId(),
                    model.getFileName(),
                    model.getAddedAt(),
                    model.getContainer(),
                    model.getCodec(),
                    metadata.getTmdbId(),
                    metadata.getOverview(),
                    metadata.getTitle(),
                    posterPath,
                    backdropPath,
                    metadata.getReleaseDate(),
                    metadata.getRuntimeMinutes()
            );
        }
    }
}
