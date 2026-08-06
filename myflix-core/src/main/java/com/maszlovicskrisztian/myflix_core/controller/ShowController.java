package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.ShowDetails;
import com.maszlovicskrisztian.myflix_core.dtos.ShowDto;
import com.maszlovicskrisztian.myflix_core.mapping.ShowMapper;
import com.maszlovicskrisztian.myflix_core.model.Show;
import com.maszlovicskrisztian.myflix_core.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/shows")
public class ShowController {

    private final ShowMapper mapper;
    private final ShowRepository showRepository;

    @GetMapping
    public List<ShowDto> getShows() {
        return showRepository.findAll().stream().map(ShowDto::from).toList();
    }

    @GetMapping("/{id}")
    public ShowDetails getShowDetails(@PathVariable Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapper.toShowDetails(show);
    }
}
