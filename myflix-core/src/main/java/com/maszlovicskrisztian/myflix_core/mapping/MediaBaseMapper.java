package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
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

    public MediaBaseResponse fromFileInfo(FileInfo model, String translatedTitle) {
        MovieMetadata movieMetadata = model.getMovieMetadata();
        if (movieMetadata != null) {
            return new MediaBaseResponse(
                    null,
                    model.getId(),
                    translatedTitle == null ? movieMetadata.getTitle() : translatedTitle,
                    imageUrlResolver.toImageUrl(movieMetadata.getBackdropPath()),
                    movieMetadata.getGenres().stream().toList()
            );
        } else if (model.getEpisodeMetadata() != null) {
            EpisodeMetadata episodeMetadata = model.getEpisodeMetadata();
            return new MediaBaseResponse(
                    episodeMetadata.getSeason().getShow().getId(),
                    model.getId(),
                    translatedTitle == null ? episodeMetadata.getTitle() : translatedTitle,
                    imageUrlResolver.toImageUrl(episodeMetadata.getStillPath()),
                    episodeMetadata.getSeason().getShow().getGenres().stream().toList()
            );
        } else {
            return new MediaBaseResponse(
                    null,
                    model.getId(),
                    model.getRelativePath(),
                    null,
                    null
            );
        }
    }
}
