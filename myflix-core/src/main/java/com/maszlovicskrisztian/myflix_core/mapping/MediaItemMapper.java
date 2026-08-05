package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaItemMapper {
    @Value("${tmdb.images.base-url}")
    private String imagesBase;

    public MediaItemDto from(FileInfo model) {
        MovieMetadata metadata = model.getMovieMetadata();

        if (metadata == null) {
            return new MediaItemDto(
                    model.getId(),
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
            String posterPath = toImageUrl(metadata.getPosterPath());
            String backdropPath = toImageUrl(metadata.getBackdropPath());

            return new MediaItemDto(
                    model.getId(),
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

    private String toImageUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        return imagesBase + "/original" + path;
    }
}
