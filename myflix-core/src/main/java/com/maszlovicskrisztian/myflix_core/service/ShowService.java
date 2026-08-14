package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaSearchResponse;
import com.maszlovicskrisztian.myflix_core.mapping.MediaSearchMapper;
import com.maszlovicskrisztian.myflix_core.model.Show;
import com.maszlovicskrisztian.myflix_core.repository.ShowRepository;
import com.maszlovicskrisztian.myflix_core.repository.projection.TitleProjection;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class ShowService {
    private final ShowRepository showRepository;
    private final MediaSearchMapper searchMapper;

    public List<Show> getShows() {
        return showRepository.findAll();
    }

    public Optional<Show> getShowById(Long id) {
        return showRepository.findById(id);
    }

    public List<MediaSearchResponse> findAllTitleWithIdByQuery(String query) {
        return showRepository.findAllBy(TitleProjection.class)
                .stream().filter(x -> x.getTitle().contains(query))
                .map(searchMapper::fromShow)
                .toList();
    }

    @Transactional
    public void deleteEmptyShows() {
        log.trace("Deleting show without episodes started");
        List<Show> emptyShows = showRepository.findAll().stream().filter(s -> s.getEpisodes().isEmpty()).toList();
        log.debug("Found {} shows without episodes.", emptyShows.size());
        showRepository.deleteAll(emptyShows);
        log.trace("Deleting show without episodes finished");
    }
}
