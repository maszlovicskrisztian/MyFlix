package com.maszlovicskrisztian.myflix_core.service;

import com.maszlovicskrisztian.myflix_core.model.Show;
import com.maszlovicskrisztian.myflix_core.repository.ShowRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ShowService {
    private final ShowRepository showRepository;

    public List<Show> getShows() {
        return showRepository.findAll();
    }

    public Optional<Show> getShowById(Long id) {
        return showRepository.findById(id);
    }

    @Transactional
    public void deleteEmptyShows() {
        List<Show> emptyShows = showRepository.findAll().stream().filter(s -> s.getEpisodes().isEmpty()).toList();
        showRepository.deleteAll(emptyShows);
    }
}
