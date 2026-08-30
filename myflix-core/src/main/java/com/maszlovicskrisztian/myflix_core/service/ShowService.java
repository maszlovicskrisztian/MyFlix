package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.ShowDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchRequest;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbSearchResult;
import com.maszlovicskrisztian.myflix_core.exception.ResourceNotFoundException;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
import com.maszlovicskrisztian.myflix_core.mapping.ShowMapper;
import com.maszlovicskrisztian.myflix_core.model.SeasonMetadata;
import com.maszlovicskrisztian.myflix_core.model.Show;
import com.maszlovicskrisztian.myflix_core.repository.SeasonMetadataRepository;
import com.maszlovicskrisztian.myflix_core.repository.ShowRepository;
import com.maszlovicskrisztian.myflix_core.repository.projection.TitleProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class ShowService {
    private final SeasonMetadataRepository seasonRepository;
    private final ShowRepository showRepository;
    private final ShowMapper mapper;
    private final MediaBaseMapper baseMapper;
    private final TranslationService translationService;
    private final TmdbClient tmdbClient;

    @Transactional(readOnly = true)
    public List<MediaBaseResponse> getShows(String languageCode) {
        var shows = showRepository.findAll();

        if (!languageCode.equalsIgnoreCase("en"))
        {
            return translationService.translateShows(shows, languageCode)
                    .stream().map(mapper::toMediaBaseResponse)
                    .sorted(Comparator.comparing(MediaBaseResponse::title))
                    .toList();
        }

        return shows
                .stream().map(mapper::toMediaBaseResponse)
                .sorted(Comparator.comparing(MediaBaseResponse::title))
                .toList();
    }

    @Transactional(readOnly = true)
    public ShowDetailsResponse getShowById(Long id, String languageCode) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find show by id: " + id));

        if (!languageCode.equalsIgnoreCase("en")) {
            return mapper.toTranslatedShowDetails(translationService.translateShowDetails(show, languageCode));
        }

        return mapper.toShowDetails(show);
    }

    public List<MediaSearchResponse> findAllTitleWithIdByQuery(String query, String languageCode) {
        if (languageCode.equalsIgnoreCase("en")) {
            return showRepository.findAllBy(TitleProjection.class)
                    .stream().filter(x -> x.getTitle().contains(query))
                    .map(mapper::toMediaSearchResponse)
                    .toList();
        }

        TmdbSearchRequest request = new TmdbSearchRequest(query, null, null, null, languageCode);
        Set<Long> tmdbIds = tmdbClient.searchTV(request).stream().map(TmdbSearchResult::id).collect(Collectors.toSet());
        List<Show> shows = showRepository
                .findAll()
                .stream().filter(x -> tmdbIds.contains(x.getTmdbId()))
                .toList();

        return translationService.translateShows(shows, languageCode).stream().map(mapper::toMediaSearchResponse).toList();
    }

    @Transactional
    public void deleteEmptyShows() {
        log.trace("Deleting seasons without episodes started");
        List<SeasonMetadata> emptySeasons = seasonRepository.findAll().stream().filter(s -> s.getEpisodes().isEmpty()).toList();
        log.debug("Found {} seasons without episodes.", emptySeasons.size());
        seasonRepository.deleteAll(emptySeasons);

        log.trace("Deleting show without episodes started");
        List<Show> emptyShows = showRepository.findAll().stream().filter(s -> s.getSeasons().isEmpty()).toList();
        log.debug("Found {} shows without episodes.", emptyShows.size());
        showRepository.deleteAll(emptyShows);

        log.trace("Deleting show without episodes finished");
    }

    public List<MediaBaseResponse> discoverShows(int monthsBack, String languageCode) {
        return tmdbClient.discoverNewShows(monthsBack, languageCode)
                .stream().map(baseMapper::fromDiscover)
                .toList();
    }
}
