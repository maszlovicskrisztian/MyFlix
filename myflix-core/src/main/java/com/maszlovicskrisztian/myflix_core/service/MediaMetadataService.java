package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.ParsedMediaTitle;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbGenre;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;
import com.maszlovicskrisztian.myflix_core.helpers.MediaTitleParser;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.model.MediaMetadata;
import com.maszlovicskrisztian.myflix_core.model.MediaType;
import com.maszlovicskrisztian.myflix_core.repository.MediaMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaMetadataService {

    private final MediaTitleParser parser;
    private final TmdbClient tmdbClient;
    private final MediaMetadataRepository metadataRepository;

    public void enrichMedia(MediaItem mediaItem) {
        if (mediaItem == null)
            return;

        if (mediaItem.getMetadata() != null)
            return;

        String relativePath = mediaItem.getRelativePath();
        ParsedMediaTitle parsedMediaTitle = parser.parseByFilename(relativePath);

        TmdbSearchResult result = tmdbClient.searchBestMatch(parsedMediaTitle.title(), parsedMediaTitle.year()).orElse(null);
        if (result == null) {
            parsedMediaTitle = parser.parseByFolder(relativePath);
            result = tmdbClient.searchBestMatch(parsedMediaTitle.title(), parsedMediaTitle.year()).orElse(null);

            if (result == null)
                return;
        }

        boolean isMovie = result.mediaType().equals(MediaType.MOVIE.name().toLowerCase());

        TmdbDetailsResponse details = null;
        if (isMovie) {
            details = tmdbClient.getMovieDetails(result.id());
        } else {
            details = tmdbClient.getTvDetails(result.id());
        }

        if (details == null)
            return;

        MediaMetadata metadata = new MediaMetadata();
        metadata.setMediaItem(mediaItem);
        metadata.setTmdbId(result.id());
        metadata.setTitle(isMovie ? details.title() : details.name());
        metadata.setReleaseDate(LocalDate.parse(details.releaseDate()));
        metadata.setRuntimeMinutes(details.runtime());
        metadata.setMediaType(MediaType.valueOf(result.mediaType().toUpperCase()));
        metadata.setGenres(details.genres().stream().map(TmdbGenre::name).collect(Collectors.toList()));
        metadata.setOverview(details.overview());
        metadata.setBackdropPath(details.backdropPath());
        metadata.setPosterPath(details.posterPath());
        metadata.setSeasonNumber(parsedMediaTitle.season());
        metadata.setEpisodeNumber(parsedMediaTitle.episode());

        metadataRepository.save(metadata);
    }
}
