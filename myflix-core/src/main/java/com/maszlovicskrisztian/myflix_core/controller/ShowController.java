package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.response.ShowDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.mapping.MediaBaseMapper;
import com.maszlovicskrisztian.myflix_core.mapping.ShowMapper;
import com.maszlovicskrisztian.myflix_core.model.Show;
import com.maszlovicskrisztian.myflix_core.service.ShowService;
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
    private final MediaBaseMapper mediaBaseMapper;
    private final ShowService showService;

    @GetMapping
    public List<MediaBaseResponse> getShows() {
        return showService.getShows().stream().map(mediaBaseMapper::fromShow).toList();
    }

    @GetMapping("/{id}")
    public ShowDetailsResponse getShowDetails(@PathVariable Long id) {
        Show show = showService.getShowById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapper.toShowDetails(show);
    }
}
