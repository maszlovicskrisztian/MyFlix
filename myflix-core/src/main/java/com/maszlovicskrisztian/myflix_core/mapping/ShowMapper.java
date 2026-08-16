package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.EpisodeDetails;
import com.maszlovicskrisztian.myflix_core.dtos.SeasonDetails;
import com.maszlovicskrisztian.myflix_core.dtos.TranslatedShowResult;
import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.ShowDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.helpers.ImageUrlResolver;
import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;
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
                translatedShow.localizedTitle().isBlank() ? show.getTitle() : translatedShow.localizedTitle(),
                imageUrlResolver.toImageUrl(show.getPosterPath()),
                show.getGenres().stream().toList()
        );
    }

    public MediaBaseResponse toMediaBaseResponse(Show model) {
        return new MediaBaseResponse(
                model.getId(),
                null,
                model.getTitle(),
                imageUrlResolver.toImageUrl(model.getPosterPath()),
                model.getGenres().stream().toList()
        );
    }

    public ShowDetailsResponse toShowDetails(TranslatedShowResult translatedShow) {
        if (translatedShow == null || translatedShow.show() == null)
            return null;

        Show model = translatedShow.show();
        List<EpisodeMetadata> episodes = model.getEpisodes().stream().toList();
        Map<Integer, List<EpisodeMetadata>> episodesPerSeason = episodes
                .stream().collect(Collectors.groupingBy(
                        EpisodeMetadata::getSeasonNumber,
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<SeasonDetails> seasons = episodesPerSeason.values().stream().map(this::toSeasonDetails).toList();

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
        List<EpisodeMetadata> episodes = model.getEpisodes().stream().toList();
        Map<Integer, List<EpisodeMetadata>> episodesPerSeason = episodes
                .stream().collect(Collectors.groupingBy(
                        EpisodeMetadata::getSeasonNumber,
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<SeasonDetails> seasons = episodesPerSeason.values().stream().map(this::toSeasonDetails).toList();

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

    public SeasonDetails toSeasonDetails(List<EpisodeMetadata> episodes) {
        EpisodeMetadata base = episodes.getFirst();

        List<EpisodeDetails> sortedEpisodes = episodes.stream()
                .sorted(Comparator.comparing(EpisodeMetadata::getEpisodeNumber))
                .map(this::toEpisodeDetails)
                .toList();

        return new SeasonDetails(
                base.getSeasonTitle(),
                base.getSeasonOverview(),
                imageUrlResolver.toImageUrl(base.getSeasonPosterPath()),
                base.getSeasonNumber(),
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
}
