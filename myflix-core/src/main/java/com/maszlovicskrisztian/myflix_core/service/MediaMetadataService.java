package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.ParsedMediaTitle;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.ImdbSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbGenre;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;
import com.maszlovicskrisztian.myflix_core.helpers.MediaTitleParser;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.model.MediaMetadata;
import com.maszlovicskrisztian.myflix_core.model.MediaType;
import com.maszlovicskrisztian.myflix_core.repository.MediaItemRepository;
import com.maszlovicskrisztian.myflix_core.repository.MediaMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaMetadataService {

    private final MediaTitleParser parser;
    private final TmdbClient tmdbClient;
    private final MediaMetadataRepository metadataRepository;
    private final MediaItemRepository mediaItemRepository;

    public void enrich() {
        List<MediaItem> mediaMissingMetadata = mediaItemRepository.findAll().stream().filter(x -> x.getMetadata() == null).toList();
        mediaMissingMetadata.forEach(this::enrichMedia);
    }

    public void enrichMediaByImdbId(Long mediaId,  String imdbId) {
        MediaItem media = mediaItemRepository.findById(mediaId).orElse(null);

        if (media == null)
            return;

        ImdbSearchResponse response = tmdbClient.searchByImdbId(imdbId);

        if (response == null)
            return;

        boolean isMovie = false;
        TmdbSearchResult result;
        TmdbDetailsResponse details;
        if (response.movieResults() != null) {
            result = response.movieResults().getFirst();
            details = tmdbClient.getMovieDetails(result.id());
            isMovie = true;
        } else {
            result = response.tvResults().getFirst();
            details = tmdbClient.getTvDetails(result.id());
        }

        saveMetadata(media, result, details, isMovie);
    }

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

        TmdbDetailsResponse details;
        if (isMovie) {
            details = tmdbClient.getMovieDetails(result.id());
        } else {
            details = tmdbClient.getTvDetails(result.id());
        }

        if (details == null)
            return;

        saveMetadata(mediaItem, result, details, isMovie);
    }

    private void saveMetadata(
            MediaItem mediaItem,
            TmdbSearchResult result,
            TmdbDetailsResponse details,
            boolean isMovie) {

        MediaMetadata metadata = mediaItem.getMetadata();
        if (metadata == null) {
            metadata = new MediaMetadata();
            metadata.setMediaItem(mediaItem);
        }

        metadata.setTmdbId(result.id());
        metadata.setTitle(isMovie ? details.title() : details.name());
        metadata.setReleaseDate(LocalDate.parse(details.releaseDate()));
        metadata.setRuntimeMinutes(details.runtime());
        metadata.setMediaType(MediaType.valueOf(result.mediaType().toUpperCase()));
        metadata.setGenres(details.genres().stream().map(TmdbGenre::name).collect(Collectors.toList()));
        metadata.setOverview(details.overview());
        metadata.setBackdropPath(details.backdropPath());
        metadata.setPosterPath(details.posterPath());
        metadata.setSeasonNumber(details.seasonNumber());
        metadata.setEpisodeNumber(details.episodeNumber());

        metadataRepository.save(metadata);
    }
}
