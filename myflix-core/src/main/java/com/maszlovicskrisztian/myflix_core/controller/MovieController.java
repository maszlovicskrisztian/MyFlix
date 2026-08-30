package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.dtos.tmdb.TmdbDiscoverResult;
import com.maszlovicskrisztian.myflix_core.interfaces.TmdbClient;
import com.maszlovicskrisztian.myflix_core.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public List<MediaBaseResponse> getMovies(@RequestParam(defaultValue = "en") String languageCode) {
        return movieService.getAllMovies(languageCode);
    }

    @GetMapping("/{id}")
    public MovieDetailsResponse getMovieById(@PathVariable Long id, @RequestParam(defaultValue = "en") String languageCode) {
        return movieService.getMovieByFileInfoId(id, languageCode);
    }

    @GetMapping("/discover")
    public List<MediaBaseResponse> discoverMovies(@RequestParam(defaultValue = "1") int monthsBack, @RequestParam(defaultValue = "en") String languageCode) {
        return movieService.discoverMovies(monthsBack, languageCode);
    }
}
