package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.ImdbSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbGenre;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;
import com.maszlovicskrisztian.myflix_core.helpers.MediaTitleParser;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.model.*;
import com.maszlovicskrisztian.myflix_core.repository.EpisodeMetadataRepository;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.MovieMetadataRepository;
import com.maszlovicskrisztian.myflix_core.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaMetadataService {

    private final MediaTitleParser parser;
    private final TmdbClient tmdbClient;
    private final MovieMetadataRepository movieMetadataRepository;
    private final EpisodeMetadataRepository episodeMetadataRepository;
    private final FileInfoRepository fileInfoRepository;
    private final ShowRepository showRepository;

    public void enrich() {
        List<FileInfo> mediaMissingMetadata = fileInfoRepository.findAll().stream().filter(x -> x.getMovieMetadata() == null && x.getEpisodeMetadata() == null).toList();
        mediaMissingMetadata.forEach(this::enrichMedia);
    }

    public void enrichMediaByImdbId(Long fileInfoId,  String imdbId) {
        FileInfo media = fileInfoRepository.findById(fileInfoId).orElse(null);

        if (media == null)
            return;

        ImdbSearchResponse response = tmdbClient.searchByImdbId(imdbId);

        if (response == null)
            return;

        TmdbSearchResult result;
        if (response.movieResults() != null) {
            result = response.movieResults().getFirst();
            enrichMovie(media, result.id());
        } else if (response.episodeResults() != null) {
            result = response.episodeResults().getFirst();
            enrichShow(result.showId(), result.season(), result.episode(), media);
        }
    }

    public void enrichMedia(FileInfo fileInfo) {
        if (fileInfo == null)
            return;

        if (fileInfo.getMovieMetadata() != null || fileInfo.getEpisodeMetadata() != null)
            return;

        try {
            Path relativePath = Paths.get(fileInfo.getRelativePath());
            String title = parser.getTitle(relativePath);

            if (title == null) return;

            TmdbSearchResult result = tmdbClient.searchBestMatch(title).orElse(null);

            if (result == null) return; //át kell még gondolni

            boolean isMovie = result.mediaType().equals(MediaType.MOVIE.name().toLowerCase());
            if (isMovie) {
                enrichMovie(fileInfo, result.id());
                return;
            }

            Integer season = parser.getSeason(relativePath);
            Integer episode = parser.getEpisode(relativePath);

            if (season == null || episode == null)
                return;

            enrichShow(result.id(), season, episode, fileInfo);
        } catch (Exception e) {
        }
    }

    private void enrichShow(Long showId, Integer season, Integer episode, FileInfo fileInfo) {
        Show savedShow = showRepository.findByTmdbId(showId).orElse(null);
        if (savedShow == null) {
            TmdbDetailsResponse showDetails = tmdbClient.getTvDetails(showId);
            Show show = new Show();
            show.setTmdbId(showId);
            show.setBackdropPath(showDetails.backdropPath());
            show.setFirstAirDate(parseDateOrNull(showDetails.firstAirDate()));
            show.setGenres(showDetails.genres().stream().map(TmdbGenre::name).collect(Collectors.toList()));
            show.setTitle(showDetails.name());
            show.setSeasonCount(showDetails.seasonCount());
            show.setEpisodeCount(showDetails.episodeCount());
            show.setOverview(showDetails.overview());
            show.setPosterPath(showDetails.posterPath());

            savedShow = showRepository.save(show);
        }

        TmdbDetailsResponse seasonDetails = tmdbClient.getTvSeasonDetails(showId, season);
        TmdbDetailsResponse episodeDetails = tmdbClient.getTvEpisodeDetails(showId, season, episode);

        EpisodeMetadata episodeMetadata = fileInfo.getEpisodeMetadata();

        if (episodeMetadata == null) {
            episodeMetadata = new EpisodeMetadata();
            episodeMetadata.setFileInfo(fileInfo);
        }

        episodeMetadata.setTmdbId(episodeDetails.id());
        episodeMetadata.setOverview(episodeDetails.overview());
        episodeMetadata.setTitle(episodeDetails.name());
        episodeMetadata.setStillPath(episodeDetails.stillPath());
        episodeMetadata.setReleaseDate(parseDateOrNull(episodeDetails.airDate()));
        episodeMetadata.setRuntimeMinutes(episodeDetails.runtime());
        episodeMetadata.setSeasonNumber(season);
        episodeMetadata.setEpisodeNumber(episode);
        episodeMetadata.setSeasonTitle(seasonDetails.name());
        episodeMetadata.setSeasonOverview(seasonDetails.overview());
        episodeMetadata.setSeasonPosterPath(seasonDetails.posterPath());
        episodeMetadata.setShow(savedShow);

        episodeMetadataRepository.save(episodeMetadata);
    }

    private void enrichMovie(FileInfo fileInfo, Long tmdbId) {
        MovieMetadata metadata = fileInfo.getMovieMetadata();

        if (metadata == null) {
            metadata = new MovieMetadata();
            metadata.setFileInfo(fileInfo);
        }

        TmdbDetailsResponse details = tmdbClient.getMovieDetails(tmdbId);

        metadata.setTmdbId(tmdbId);
        metadata.setTitle(details.title());
        metadata.setReleaseDate(parseDateOrNull(details.releaseDate()));
        metadata.setRuntimeMinutes(details.runtime());
        metadata.setGenres(details.genres().stream().map(TmdbGenre::name).collect(Collectors.toList()));
        metadata.setOverview(details.overview());
        metadata.setBackdropPath(details.backdropPath());
        metadata.setPosterPath(details.posterPath());

        movieMetadataRepository.save(metadata);
    }

    private LocalDate parseDateOrNull(String date) {
        if (date ==  null) return null;

        return LocalDate.parse(date);
    }
}
