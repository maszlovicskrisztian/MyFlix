package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.MediaItemDto;
import com.maszlovicskrisztian.myflix_core.helpers.ImageUrlResolver;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MediaItemMapper {
    private final ImageUrlResolver imageUrlResolver;

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
            return new MediaItemDto(
                    model.getId(),
                    model.getAddedAt(),
                    model.getContainer(),
                    model.getCodec(),
                    metadata.getTmdbId(),
                    metadata.getOverview(),
                    metadata.getTitle(),
                    imageUrlResolver.toImageUrl(metadata.getPosterPath()),
                    imageUrlResolver.toImageUrl(metadata.getBackdropPath()),
                    metadata.getReleaseDate(),
                    metadata.getRuntimeMinutes()
            );
        }
    }
}
