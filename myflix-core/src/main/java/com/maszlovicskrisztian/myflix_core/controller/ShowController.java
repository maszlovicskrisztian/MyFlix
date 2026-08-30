package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.response.ShowDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDiscoverResult;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/shows")
public class ShowController {

    private final ShowService showService;
    private final TmdbClient tmdbClient;

    @GetMapping
    public List<MediaBaseResponse> getShows(@RequestParam String languageCode) {
        return showService.getShows(languageCode);
    }

    @GetMapping("/{id}")
    public ShowDetailsResponse getShowDetails(@PathVariable Long id, @RequestParam String languageCode) {
        return showService.getShowById(id, languageCode);
    }

    @GetMapping("/discover")
    public List<TmdbDiscoverResult> discoverShows(@RequestParam(defaultValue = "1") int monthsBack) {
        return tmdbClient.discoverNewShows(monthsBack);
    }
}
