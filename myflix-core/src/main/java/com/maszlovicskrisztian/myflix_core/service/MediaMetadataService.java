package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.tmdb.*;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import com.maszlovicskrisztian.myflix_core.helpers.MediaTitleParser;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.model.*;
import com.maszlovicskrisztian.myflix_core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaMetadataService {

    private final MediaTitleParser parser;
    private final TmdbClient tmdbClient;
    private final MovieMetadataRepository movieMetadataRepository;
    private final EpisodeMetadataRepository episodeMetadataRepository;
    private final FileInfoRepository fileInfoRepository;
    private final ShowRepository showRepository;
    private final SeasonMetadataRepository seasonRepository;

    public void enrich() {
        List<FileInfo> mediaMissingMetadata = fileInfoRepository
                .findAll()
                .stream().filter(x -> x.getMovieMetadata() == null && x.getEpisodeMetadata() == null)
                .toList();

        mediaMissingMetadata.forEach(this::enrichMedia);
    }

    public void enrichMediaByImdbId(Long fileInfoId,  String imdbId) {
        log.trace("Enriching media by Imdb started.");
        FileInfo media = fileInfoRepository.findById(fileInfoId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find file info by id: " + fileInfoId));

        ImdbSearchResponse response = tmdbClient.searchByImdbId(imdbId);

        if (response == null) {
            log.info("TMDB query for Imdb id: {} returned no result. Enriching is not possible.", imdbId);
            return;
        }

        TmdbSearchResult result;
        if (!response.movieResults().isEmpty()) {
            result = response.movieResults().getFirst();
            enrichMovie(media, result.id());
        } else if (!response.episodeResults().isEmpty()) {
            result = response.episodeResults().getFirst();
            enrichShow(result.showId(), result.season(), result.episode(), media);
        } else {
            log.info("TMDB response for Imdb id {} had neither movie nor episode results", imdbId);
        }

        log.trace("Enriching media by Imdb finished.");
    }

    public void enrichMedia(FileInfo fileInfo) {
        if (fileInfo == null)
            return;

        log.trace("Automatic enrich started for file info: {}", fileInfo.getId());

        if (fileInfo.getMovieMetadata() != null || fileInfo.getEpisodeMetadata() != null) {
            log.warn("File info: {} already enriched, automatic enrich not possible.", fileInfo.getId());
            return;
        }

        try {
            Path relativePath = Paths.get(fileInfo.getRelativePath());
            TmdbSearchRequest parseResult = parser.getSearchDetailsFromPath(relativePath);
            TmdbSearchResult result = tmdbClient.searchBestMatch(parseResult);

            if (result == null) {
                log.warn("TMDB query for title: {} year: {} returned no result. Enriching is not possible.", parseResult.title(), parseResult.year());
                return;
            }

            boolean isMovie = result.showTitle() == null;
            if (isMovie) {
                enrichMovie(fileInfo, result.id());
                return;
            }

            if (parseResult.season() == null || parseResult.episode() == null){
                log.warn("TMDB query for title: {} returned a show but season and/or episode could not be resolved. Enriching is not possible.", parseResult.title());
                return;
            }

            enrichShow(result.id(), parseResult.season(), parseResult.episode(), fileInfo);

            log.trace("Automatic enrich finished for file info: {}", fileInfo.getId());
        } catch (Exception e) {
            log.error("Error during automatic enrich for file info: {}: {}", fileInfo.getId(), e.getMessage());
        }
    }

    private void enrichShow(Long showId, Integer season, Integer episode, FileInfo fileInfo) {
        Show savedShow = showRepository.findByTmdbId(showId).orElse(null);
        if (savedShow == null) {
            TmdbShowDetailsResponse showDetails = tmdbClient.getTvDetails(showId, "en");
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
            log.info("New show saved: {}", showDetails.name());
        }

        SeasonMetadata savedSeason = seasonRepository.findByShowIdAndSeasonNumber(savedShow.getId(), season).orElse(null);
        if (savedSeason == null) {
            TmdbSeasonDetailsResponse seasonDetails = tmdbClient.getTvSeasonDetails(showId, season, "en");
            SeasonMetadata seasonMetadata = new SeasonMetadata();
            seasonMetadata.setTmdbId(seasonDetails.id());
            seasonMetadata.setPosterPath(seasonDetails.posterPath());
            seasonMetadata.setTitle(seasonDetails.name());
            seasonMetadata.setOverview(seasonDetails.overview());
            seasonMetadata.setReleaseDate(parseDateOrNull(seasonDetails.airDate()));
            seasonMetadata.setSeasonNumber(season);
            seasonMetadata.setShow(savedShow);

            savedSeason = seasonRepository.save(seasonMetadata);
            log.info("New season saved: {}", seasonDetails.name());
        }

        EpisodeMetadata episodeMetadata = fileInfo.getEpisodeMetadata();
        if (episodeMetadata == null) {
            TmdbEpisodeDetailsResponse episodeDetails = tmdbClient.getTvEpisodeDetails(showId, season, episode, "en");
            episodeMetadata = new EpisodeMetadata();
            episodeMetadata.setFileInfo(fileInfo);
            episodeMetadata.setTmdbId(episodeDetails.id());
            episodeMetadata.setOverview(episodeDetails.overview());
            episodeMetadata.setTitle(episodeDetails.name());
            episodeMetadata.setStillPath(episodeDetails.stillPath());
            episodeMetadata.setReleaseDate(parseDateOrNull(episodeDetails.airDate()));
            episodeMetadata.setRuntimeMinutes(episodeDetails.runtime());
            episodeMetadata.setEpisodeNumber(episode);
            episodeMetadata.setSeason(savedSeason);

            episodeMetadataRepository.save(episodeMetadata);
            log.info("{}: season {} episode {} saved successfully", savedShow.getTitle(), season, episode);
        }
    }

    private void enrichMovie(FileInfo fileInfo, Long tmdbId) {
        MovieMetadata metadata = fileInfo.getMovieMetadata();

        if (metadata == null) {
            metadata = new MovieMetadata();
            metadata.setFileInfo(fileInfo);
        }

        TmdbMovieDetailsResponse details = tmdbClient.getMovieDetails(tmdbId, "en");

        metadata.setTmdbId(tmdbId);
        metadata.setTitle(details.title());
        metadata.setReleaseDate(parseDateOrNull(details.releaseDate()));
        metadata.setRuntimeMinutes(details.runtime());
        metadata.setGenres(details.genres().stream().map(TmdbGenre::name).collect(Collectors.toList()));
        metadata.setOverview(details.overview());
        metadata.setBackdropPath(details.backdropPath());
        metadata.setPosterPath(details.posterPath());

        movieMetadataRepository.save(metadata);
        log.info("New movie saved: {}", details.title());
    }

    private LocalDate parseDateOrNull(String date) {
        if (date ==  null || date.isEmpty()) return null;

        return LocalDate.parse(date);
    }
}
