package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.TranslatedEpisodeResult;
import com.maszlovicskrisztian.myflix_core.dtos.TranslatedMovieResult;
import com.maszlovicskrisztian.myflix_core.dtos.TranslatedSeasonResult;
import com.maszlovicskrisztian.myflix_core.dtos.TranslatedShowResult;
import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbEpisodeDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbMovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSeasonDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbShowDetailsResponse;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.model.*;
import com.maszlovicskrisztian.myflix_core.repository.MetadataTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranslationService {
    private final TmdbClient tmdbClient;
    private final MetadataTranslationRepository translationRepository;

    public List<TranslatedShowResult> translateShows(List<Show> shows, String languageCode) {
        return shows.stream().map(x -> translateShow(x, languageCode)).toList();
    }

    public TranslatedShowResult translateShow(Show show, String languageCode) {
        MetadataTranslation translation = translationRepository
                .findByEntityTypeAndEntityIdAndLanguageCode(MediaType.TV, show.getId(), languageCode)
                .orElse(null);

        if (translation == null) {
            TmdbShowDetailsResponse translatedResult = tmdbClient.getTvDetails(show.getTmdbId(), languageCode);
            translation = saveTranslation(MediaType.TV, show.getId(), translatedResult.name(), translatedResult.overview(), languageCode);
        }

        return new TranslatedShowResult(show, translation.getTitle(), translation.getOverview(), null);
    }

    public TranslatedShowResult translateShowDetails(Show show, String languageCode) {
        TranslatedShowResult showTranslation = translateShow(show, languageCode);
        List<TranslatedSeasonResult> translatedSeasons = new ArrayList<>();

        show.getSeasons().forEach((x) -> {
            translatedSeasons.add(translateSeason(show.getTmdbId(), x, languageCode));
        });

        return new TranslatedShowResult(show, showTranslation.localizedTitle(), showTranslation.localizedOverview(), translatedSeasons);
    }

    private TranslatedSeasonResult translateSeason(Long showTmdbId, SeasonMetadata seasonMetadata, String languageCode) {
        MetadataTranslation translation = translationRepository
                .findByEntityTypeAndEntityIdAndLanguageCode(MediaType.TV_SEASON, seasonMetadata.getId(), languageCode)
                .orElse(null);

        if (translation == null) {
            TmdbSeasonDetailsResponse translatedSeason = tmdbClient.getTvSeasonDetails(showTmdbId, seasonMetadata.getSeasonNumber(), languageCode);
            translation = saveTranslation(MediaType.TV_SEASON, seasonMetadata.getId(), translatedSeason.name(), translatedSeason.overview(), languageCode);
        }

        List<TranslatedEpisodeResult> translatedEpisodes = new ArrayList<>();
        seasonMetadata.getEpisodes().forEach((x) -> {
            translatedEpisodes.add(translateEpisode(showTmdbId, x, seasonMetadata.getSeasonNumber(), languageCode));
        });

        return new TranslatedSeasonResult(seasonMetadata, translation.getTitle(), translation.getOverview(), translatedEpisodes);
    }

    private TranslatedEpisodeResult translateEpisode(Long showTmdbId, EpisodeMetadata episodeMetadata, Integer seasonNumber, String languageCode) {
        MetadataTranslation translation = translationRepository
                .findByEntityTypeAndEntityIdAndLanguageCode(MediaType.TV_EPISODE, episodeMetadata.getId(), languageCode)
                .orElse(null);

        if (translation == null) {
            TmdbEpisodeDetailsResponse translatedEpisode = tmdbClient.getTvEpisodeDetails(showTmdbId, seasonNumber, episodeMetadata.getEpisodeNumber(), languageCode);
            translation = saveTranslation(MediaType.TV_EPISODE, episodeMetadata.getId(), translatedEpisode.name(), translatedEpisode.overview(), languageCode);
        }

        return new TranslatedEpisodeResult(episodeMetadata, translation.getTitle(), translation.getOverview());
    }

    public List<TranslatedMovieResult> translateMovies(List<MovieMetadata> movies, String languageCode) {
        return movies.stream().map(x -> translateMovie(x, languageCode)).toList();
    }

    public TranslatedMovieResult translateMovie(MovieMetadata movieMetadata, String languageCode) {
        if (movieMetadata == null)
            return null;

        MetadataTranslation translation = translationRepository
                .findByEntityTypeAndEntityIdAndLanguageCode(MediaType.MOVIE, movieMetadata.getId(), languageCode)
                .orElse(null);

        if (translation == null) {
            TmdbMovieDetailsResponse translatedResult = tmdbClient.getMovieDetails(movieMetadata.getTmdbId(), languageCode);
            translation = saveTranslation(MediaType.MOVIE, movieMetadata.getId(), translatedResult.title(), translatedResult.overview(), languageCode);
        }

        return new TranslatedMovieResult(movieMetadata, translation.getTitle(), translation.getOverview());
    }

    public String translateMediaTitle(FileInfo media, String languageCode) {
        if (media == null)
            return null;

        String translatedTitle;
        if (media.getMovieMetadata() == null)
            translatedTitle = translateShow(media.getEpisodeMetadata().getSeason().getShow(), languageCode).localizedTitle();
        else if (media.getEpisodeMetadata() == null)
            translatedTitle = translateMovie(media.getMovieMetadata(), languageCode).localizedTitle();
        else
            translatedTitle = "";

        return translatedTitle;
    }

    private MetadataTranslation saveTranslation(MediaType entityType, Long entityId, String title, String overview, String languageCode) {
        MetadataTranslation translation = new MetadataTranslation();
        translation.setEntityType(entityType);
        translation.setEntityId(entityId);
        translation.setTitle(title);
        translation.setOverview(overview);
        translation.setLanguageCode(languageCode);

        return translationRepository.save(translation);
    }
}
