package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.*;
import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.ShowDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.helpers.ImageUrlResolver;
import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;
import com.maszlovicskrisztian.myflix_core.model.SeasonMetadata;
import com.maszlovicskrisztian.myflix_core.model.Show;
import com.maszlovicskrisztian.myflix_core.repository.projection.TitleProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShowMapper {
    private final ImageUrlResolver imageUrlResolver;

    public MediaSearchResponse toMediaSearchResponse(TranslatedShowResult translatedShow) {
        if (translatedShow == null || translatedShow.show() == null)
            return null;

        Show show = translatedShow.show();
        return new MediaSearchResponse(
                show.getId(),
                translatedShow.localizedTitle().isBlank() ? show.getTitle() : translatedShow.localizedTitle(),
                MediaType.TV.name());
    }

    public MediaSearchResponse toMediaSearchResponse(TitleProjection projection) {
        return new MediaSearchResponse(projection.getId(), projection.getTitle(), MediaType.TV.name());
    }

    public MediaBaseResponse toMediaBaseResponse(TranslatedShowResult translatedShow) {
        if (translatedShow == null || translatedShow.show() == null)
            return null;

        Show show = translatedShow.show();

        return new MediaBaseResponse(
                show.getId(),
                null,
                show.getTmdbId(),
                translatedShow.localizedTitle().isBlank() ? show.getTitle() : translatedShow.localizedTitle(),
                imageUrlResolver.toImageUrl(show.getPosterPath()),
                show.getOverview(),
                show.getGenres().stream().toList()
        );
    }

    public MediaBaseResponse toMediaBaseResponse(Show model) {
        return new MediaBaseResponse(
                model.getId(),
                null,
                model.getTmdbId(),
                model.getTitle(),
                imageUrlResolver.toImageUrl(model.getPosterPath()),
                model.getOverview(),
                model.getGenres().stream().toList()
        );
    }

    public ShowDetailsResponse toTranslatedShowDetails(TranslatedShowResult translatedShow) {
        if (translatedShow == null || translatedShow.show() == null)
            return null;

        Show model = translatedShow.show();
        List<SeasonDetails> seasons = translatedShow.seasons().stream().map(this::toTranslatedSeasonDetails).toList();

        return new ShowDetailsResponse(
                model.getId(),
                translatedShow.localizedTitle().isBlank() ? model.getTitle() : translatedShow.localizedTitle(),
                translatedShow.localizedOverview().isBlank() ? model.getOverview() : translatedShow.localizedOverview(),
                imageUrlResolver.toImageUrl(model.getPosterPath()),
                imageUrlResolver.toImageUrl(model.getBackdropPath()),
                model.getSeasonCount(),
                model.getEpisodeCount(),
                seasons,
                model.getGenres().stream().toList()
        );
    }

    public ShowDetailsResponse toShowDetails(Show model) {
        List<SeasonDetails> seasons = model.getSeasons().stream().map(this::toSeasonDetails).toList();

        return new ShowDetailsResponse(
                model.getId(),
                model.getTitle(),
                model.getOverview(),
                imageUrlResolver.toImageUrl(model.getPosterPath()),
                imageUrlResolver.toImageUrl(model.getBackdropPath()),
                model.getSeasonCount(),
                model.getEpisodeCount(),
                seasons,
                model.getGenres().stream().toList()
        );
    }

    public SeasonDetails toSeasonDetails(SeasonMetadata season) {
        List<EpisodeDetails> sortedEpisodes = season.getEpisodes().stream()
                .sorted(Comparator.comparing(EpisodeMetadata::getEpisodeNumber))
                .map(this::toEpisodeDetails)
                .toList();

        return new SeasonDetails(
                season.getTitle(),
                season.getOverview(),
                imageUrlResolver.toImageUrl(season.getPosterPath()),
                season.getSeasonNumber(),
                sortedEpisodes
        );
    }

    public SeasonDetails toTranslatedSeasonDetails(TranslatedSeasonResult translatedSeason) {
        SeasonMetadata model = translatedSeason.season();
        List<EpisodeDetails> sortedEpisodes = translatedSeason.episodes().stream()
                .sorted(Comparator.comparing((x) -> x.episode().getEpisodeNumber()))
                .map(this::toTranslatedEpisodeDetails)
                .toList();

        return new SeasonDetails(
                translatedSeason.localizedTitle().isBlank() ? model.getTitle() : translatedSeason.localizedTitle(),
                translatedSeason.localizedOverview().isBlank() ? model.getOverview() : translatedSeason.localizedOverview(),
                imageUrlResolver.toImageUrl(model.getPosterPath()),
                model.getSeasonNumber(),
                sortedEpisodes
        );
    }

    public EpisodeDetails toEpisodeDetails(EpisodeMetadata model) {
        return new EpisodeDetails(
                model.getTitle(),
                model.getOverview(),
                imageUrlResolver.toImageUrl(model.getStillPath()),
                model.getReleaseDate(),
                model.getRuntimeMinutes(),
                model.getEpisodeNumber(),
                model.getFileInfo().getId()
        );
    }

    public EpisodeDetails toTranslatedEpisodeDetails(TranslatedEpisodeResult model) {
        return new EpisodeDetails(
                model.localizedTitle(),
                model.localizedOverview(),
                imageUrlResolver.toImageUrl(model.episode().getStillPath()),
                model.episode().getReleaseDate(),
                model.episode().getRuntimeMinutes(),
                model.episode().getEpisodeNumber(),
                model.episode().getFileInfo().getId()
        );
    }
}
