package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.response.MovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.helpers.ImageUrlResolver;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MovieMapper {
    private final ImageUrlResolver imageUrlResolver;

    public MovieDetailsResponse toMovieDetails(FileInfo model) {
        MovieMetadata metadata = model.getMovieMetadata();

        if (metadata == null)
            return null;

        return new MovieDetailsResponse(
                model.getId(),
                model.getAddedAt(),
                metadata.getOverview(),
                metadata.getTitle(),
                imageUrlResolver.toImageUrl(metadata.getPosterPath()),
                imageUrlResolver.toImageUrl(metadata.getBackdropPath()),
                metadata.getReleaseDate(),
                metadata.getRuntimeMinutes()
        );
    }
}
