package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDiscoverResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDiscoverResult;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbGenre;
import com.maszlovicskrisztian.myflix_core.helpers.ImageUrlResolver;
import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MediaBaseMapper {

    private final ImageUrlResolver imageUrlResolver;

    public MediaBaseResponse fromDiscover(TmdbDiscoverResult discoverResult) {
        return new MediaBaseResponse(
                null,
                null,
                discoverResult.tmdbId(),
                discoverResult.title() == null ? discoverResult.name() : discoverResult.title(),
                imageUrlResolver.toImageUrl(discoverResult.posterPath()),
                null
        );
    }

    public MediaBaseResponse fromFileInfo(FileInfo model, String translatedTitle) {
        MovieMetadata movieMetadata = model.getMovieMetadata();
        if (movieMetadata != null) {
            return new MediaBaseResponse(
                    null,
                    model.getId(),
                    movieMetadata.getTmdbId(),
                    translatedTitle == null ? movieMetadata.getTitle() : translatedTitle,
                    imageUrlResolver.toImageUrl(movieMetadata.getBackdropPath()),
                    movieMetadata.getGenres().stream().toList()
            );
        } else if (model.getEpisodeMetadata() != null) {
            EpisodeMetadata episodeMetadata = model.getEpisodeMetadata();
            return new MediaBaseResponse(
                    episodeMetadata.getSeason().getShow().getId(),
                    model.getId(),
                    episodeMetadata.getTmdbId(),
                    translatedTitle == null ? episodeMetadata.getTitle() : translatedTitle,
                    imageUrlResolver.toImageUrl(episodeMetadata.getStillPath()),
                    episodeMetadata.getSeason().getShow().getGenres().stream().toList()
            );
        } else {
            return new MediaBaseResponse(
                    null,
                    model.getId(),
                    null,
                    model.getRelativePath(),
                    null,
                    null
            );
        }
    }
}
