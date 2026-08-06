package com.maszlovicskrisztian.myflix_core.mapping;

import com.maszlovicskrisztian.myflix_core.dtos.EpisodeDetails;
import com.maszlovicskrisztian.myflix_core.dtos.SeasonDetails;
import com.maszlovicskrisztian.myflix_core.dtos.response.ShowDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.helpers.ImageUrlResolver;
import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;
import com.maszlovicskrisztian.myflix_core.model.Show;
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

    public ShowDetailsResponse toShowDetails(Show model) {
        List<EpisodeMetadata> episodes = model.getEpisodes();
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
                model.getGenres()
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
