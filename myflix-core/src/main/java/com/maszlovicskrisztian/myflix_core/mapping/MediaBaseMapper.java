package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.helpers.ImageUrlResolver;
import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import com.maszlovicskrisztian.myflix_core.model.Show;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MediaBaseMapper {

    private final ImageUrlResolver imageUrlResolver;

    public MediaBaseResponse fromShow(Show model) {
        return new MediaBaseResponse(
                model.getId(),
                null,
                model.getTitle(),
                imageUrlResolver.toImageUrl(model.getPosterPath())
        );
    }

    public MediaBaseResponse fromMovie(FileInfo model) {
        MovieMetadata metadata = model.getMovieMetadata();
        if (metadata == null)
            return null;

        return new MediaBaseResponse(
                null,
                model.getId(),
                metadata.getTitle(),
                imageUrlResolver.toImageUrl(metadata.getPosterPath())
        );
    }

    public MediaBaseResponse fromContinueWatching(FileInfo model) {
        MovieMetadata movieMetadata = model.getMovieMetadata();
        if (movieMetadata != null) {
            return new MediaBaseResponse(
                    null,
                    model.getId(),
                    movieMetadata.getTitle(),
                    imageUrlResolver.toImageUrl(movieMetadata.getBackdropPath())
            );
        } else {
            EpisodeMetadata episodeMetadata = model.getEpisodeMetadata();
            if (episodeMetadata != null) {
                return new MediaBaseResponse(
                        episodeMetadata.getShow().getId(),
                        model.getId(),
                        episodeMetadata.getTitle(),
                        imageUrlResolver.toImageUrl(episodeMetadata.getStillPath())
                );
            }
        }

        return null;
    }

    public MediaBaseResponse fromUnknownMedia(FileInfo model) {
        return new MediaBaseResponse(
                null,
                model.getId(),
                model.getRelativePath(),
                null
        );
    }
}
