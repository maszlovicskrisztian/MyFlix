package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.TranslatedMovieResult;
import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
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

    public MediaSearchResponse toMediaSearchResponse(TranslatedMovieResult translatedMovie) {
        if (translatedMovie == null || translatedMovie.movie() == null)
            return null;

        MovieMetadata metadata = translatedMovie.movie();
        return new MediaSearchResponse(
                translatedMovie.movie().getFileInfo().getId(),
                translatedMovie.localizedTitle().isBlank() ? metadata.getTitle() : translatedMovie.localizedTitle(),
                MediaType.MOVIE.name());
    }

    public MediaSearchResponse toMediaSearchResponse(MovieMetadata movie) {
        return new MediaSearchResponse(movie.getFileInfo().getId(), movie.getTitle(), MediaType.MOVIE.name());
    }

    public MediaBaseResponse toMediaBaseResponse(TranslatedMovieResult translatedMovie) {
        if (translatedMovie == null || translatedMovie.movie() == null)
            return null;

        MovieMetadata metadata = translatedMovie.movie();

        return new MediaBaseResponse(
                null,
                translatedMovie.movie().getFileInfo().getId(),
                metadata.getTmdbId(),
                translatedMovie.localizedTitle().isBlank() ? metadata.getTitle() : translatedMovie.localizedTitle(),
                imageUrlResolver.toImageUrl(metadata.getPosterPath()),
                metadata.getOverview(),
                metadata.getGenres().stream().toList()
        );
    }

    public MediaBaseResponse toMediaBaseResponse(MovieMetadata metadata) {
        if (metadata == null)
            return null;

        return new MediaBaseResponse(
                null,
                metadata.getFileInfo().getId(),
                metadata.getTmdbId(),
                metadata.getTitle(),
                imageUrlResolver.toImageUrl(metadata.getPosterPath()),
                metadata.getOverview(),
                metadata.getGenres().stream().toList()
        );
    }

    public MovieDetailsResponse toMovieDetails(TranslatedMovieResult translatedMovie) {
        if (translatedMovie == null || translatedMovie.movie() == null)
            return null;

        MovieMetadata metadata = translatedMovie.movie();
        return new MovieDetailsResponse(
                translatedMovie.movie().getId(),
                translatedMovie.movie().getFileInfo().getAddedAt(),
                translatedMovie.localizedOverview().isBlank() ? metadata.getOverview() : translatedMovie.localizedOverview(),
                translatedMovie.localizedTitle().isBlank() ? metadata.getTitle() : translatedMovie.localizedTitle(),
                imageUrlResolver.toImageUrl(metadata.getPosterPath()),
                imageUrlResolver.toImageUrl(metadata.getBackdropPath()),
                metadata.getReleaseDate(),
                metadata.getRuntimeMinutes()
        );
    }

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
