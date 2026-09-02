package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.dtos.arr.QualityProfile;
import com.maszlovicskrisztian.myflix_core.dtos.response.MediaBaseResponse;
import com.maszlovicskrisztian.myflix_core.dtos.response.MovieDetailsResponse;
import com.maszlovicskrisztian.myflix_core.interfaces.RadarrApiClient;
import com.maszlovicskrisztian.myflix_core.service.DiscoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController()
@RequestMapping("/api/discover")
public class DiscoverController {
    private final DiscoverService discoverService;
    private final RadarrApiClient radarrClient;

    @GetMapping("/movies")
    public List<MediaBaseResponse> discoverMovies(@RequestParam(defaultValue = "1") int monthsBack, @RequestParam(defaultValue = "en") String languageCode) {
        return discoverService.discoverMovies(monthsBack, languageCode);
    }

    @GetMapping("/movies/{tmdbId}")
    public MovieDetailsResponse getDiscoveredMovieByTmdbId(@PathVariable Long tmdbId, @RequestParam(defaultValue = "en") String languageCode) {
        return discoverService.discoveredMovieByTmdbId(tmdbId, languageCode);
    }

    @GetMapping("/movies/quality-profiles")
    public List<QualityProfile> getMovieQualityProfiles() {
        return radarrClient.getQualityProfiles();
    }

    @PostMapping("/movies/{tmdbId}")
    public ResponseEntity<Void> requestMovie(
            @PathVariable int tmdbId,
            @RequestParam String title,
            @RequestParam int qualityProfileId) {

        radarrClient.requestMovie(tmdbId, title, qualityProfileId);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/shows")
    public List<MediaBaseResponse> discoverShows(@RequestParam(defaultValue = "1") int monthsBack, @RequestParam(defaultValue = "en") String languageCode) {
        return discoverService.discoverShows(monthsBack, languageCode);
    }
}
