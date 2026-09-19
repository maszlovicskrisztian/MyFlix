package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import com.maszlovicskrisztian.myflix_core.dtos.request.MetadataUpdateRequest;
import com.maszlovicskrisztian.myflix_core.dtos.response.MetadataDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.*;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import com.maszlovicskrisztian.myflix_core.helpers.MediaTitleParser;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.model.*;
import com.maszlovicskrisztian.myflix_core.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public MetadataDetailsResponse getMetadata(Long fileInfoId) {
        FileInfo media = fileInfoRepository
                .findById(fileInfoId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find media with the requested id"));

        MovieMetadata movie = media.getMovieMetadata();
        if (movie != null) {
            return new MetadataDetailsResponse(
                    fileInfoId,
                    MediaType.MOVIE,
                    movie.getTmdbId(),
                    movie.getTitle(),
                    movie.getOverview(),
                    movie.getBackdropPath(),
                    movie.getPosterPath(),
                    movie.getReleaseDate(),
                    movie.getRuntimeMinutes(),
                    movie.getGenres() == null ? List.of() : List.copyOf(movie.getGenres()),
                    null,
                    null,
                    null);
        }

        EpisodeMetadata episode = media.getEpisodeMetadata();
        if (episode != null) {
            SeasonMetadata season = episode.getSeason();
            Show show = season.getShow();

            return new MetadataDetailsResponse(
                    fileInfoId,
                    MediaType.EPISODE,
                    episode.getTmdbId(),
                    episode.getTitle(),
                    episode.getOverview(),
                    // The editor calls the episode still a backdrop; it is the same field.
                    episode.getStillPath(),
                    null,
                    episode.getReleaseDate(),
                    episode.getRuntimeMinutes(),
                    show.getGenres() == null ? List.of() : List.copyOf(show.getGenres()),
                    show.getId(),
                    season.getSeasonNumber(),
                    episode.getEpisodeNumber());
        }

        return new MetadataDetailsResponse(
                fileInfoId, null, null, null, null, null, null, null, null, List.of(), null, null, null);
    }

    @Transactional
    public void updateMetadata(Long fileInfoId, MetadataUpdateRequest request) {
        FileInfo media = fileInfoRepository
                .findById(fileInfoId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find media with the requested id"));

        if (request.getMediaType() == MediaType.MOVIE)
            saveMovieMetadata(media, request);
        else
            saveEpisodeMetadata(media, request);
    }

    private void saveMovieMetadata(FileInfo media, MetadataUpdateRequest request) {
        if (request.getMediaType() != MediaType.MOVIE)
            return;

        EpisodeMetadata episodeMetadata = media.getEpisodeMetadata();
        if (episodeMetadata != null) {
            media.setEpisodeMetadata(null);
            episodeMetadataRepository.delete(episodeMetadata);
        }

        MovieMetadata metadata = media.getMovieMetadata();
        if (metadata == null) {
            metadata = new MovieMetadata();
            metadata.setFileInfo(media);
        }

        metadata.setBackdropPath(request.getBackdropPath());
        metadata.setOverview(request.getOverview());
        metadata.setTitle(request.getTitle());
        metadata.setGenres(request.getGenres());
        metadata.setTmdbId(request.getTmdbId());
        metadata.setPosterPath(request.getPosterPath());
        metadata.setReleaseDate(request.getReleaseDate());
        metadata.setRuntimeMinutes(request.getRuntimeMinutes());

        movieMetadataRepository.save(metadata);
    }

    private void saveEpisodeMetadata(FileInfo media, MetadataUpdateRequest request) {
        if (request.getMediaType() != MediaType.EPISODE)
            return;

        if (request.getSeasonNumber() == null || request.getEpisodeNumber() == null)
            throw new IllegalArgumentException("Requested update to an episode but season and/or episode are not present.");

        Show show = showRepository.findById(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Could not find show with the provided data"));

        SeasonMetadata season = show.getSeasons()
                .stream().filter(x -> x.getSeasonNumber().equals(request.getSeasonNumber())).toList()
                .getFirst();

        MovieMetadata movieMetadata = media.getMovieMetadata();
        if (movieMetadata != null) {
            media.setMovieMetadata(null);
            movieMetadataRepository.delete(movieMetadata);
        }

        EpisodeMetadata metadata = media.getEpisodeMetadata();
        if (metadata == null) {
            metadata = new EpisodeMetadata();
            metadata.setFileInfo(media);
        }

        metadata.setTitle(request.getTitle());
        metadata.setOverview(request.getOverview());
        metadata.setTmdbId(request.getTmdbId());
        metadata.setRuntimeMinutes(request.getRuntimeMinutes());
        metadata.setReleaseDate(request.getReleaseDate());
        metadata.setStillPath(request.getBackdropPath());
        metadata.setEpisodeNumber(request.getEpisodeNumber());
        metadata.setSeason(season);
        episodeMetadataRepository.save(metadata);
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
