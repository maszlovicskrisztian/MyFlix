package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.TranslatedMovieResult;
import com.maszlovicskrisztian.myflix_core.dtos.TranslatedShowResult;
import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDetailsResponse;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.model.FileInfo;
import com.maszlovicskrisztian.myflix_core.model.MetadataTranslation;
import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import com.maszlovicskrisztian.myflix_core.model.Show;
import com.maszlovicskrisztian.myflix_core.repository.FileInfoRepository;
import com.maszlovicskrisztian.myflix_core.repository.MetadataTranslationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
            TmdbDetailsResponse translatedResult = tmdbClient.getTvDetails(show.getTmdbId(), languageCode);
            translation = saveTranslation(MediaType.TV, show.getId(), translatedResult.name(), translatedResult.overview(), languageCode);
        }

        return new TranslatedShowResult(show, translation.getTitle(), translation.getOverview());
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
            TmdbDetailsResponse translatedResult = tmdbClient.getMovieDetails(movieMetadata.getTmdbId(), languageCode);
            translation = saveTranslation(MediaType.MOVIE, movieMetadata.getId(), translatedResult.title(), translatedResult.overview(), languageCode);
        }

        return new TranslatedMovieResult(movieMetadata, translation.getTitle(), translation.getOverview());
    }

    public String translateMediaTitle(FileInfo media, String languageCode) {
        if (media == null)
            return null;

        String translatedTitle;
        if (media.getMovieMetadata() == null)
            translatedTitle = translateShow(media.getEpisodeMetadata().getShow(), languageCode).localizedTitle();
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
