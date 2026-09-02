package com.maszlovicskrisztian.myflix_core.interfaces;

import com.maszlovicskrisztian.myflix_core.dtos.arr.AddOptions;
import com.maszlovicskrisztian.myflix_core.dtos.arr.QualityProfile;
import com.maszlovicskrisztian.myflix_core.dtos.arr.RadarrRequest;
import com.maszlovicskrisztian.myflix_core.dtos.arr.RootFolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RadarrApiClient {
    private final RestClient radarrClient;

    public List<QualityProfile> getQualityProfiles() {
        return radarrClient.get()
                .uri("/api/v3/qualityprofile")
                .retrieve()
                .body(new ParameterizedTypeReference<List<QualityProfile>>() {});
    }

    public void requestMovie(int tmdbId, String title, int qualityProfileId) {
        var request = new RadarrRequest(
                tmdbId,
                title,
                qualityProfileId,
                resolveRootFolderPath(),
                true,
                "released",
                new AddOptions(true));

        radarrClient.post()
                .uri("/api/v3/movie")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private String resolveRootFolderPath() {
        List<RootFolder> folders = radarrClient.get()
                .uri("/api/v3/rootfolder")
                .retrieve()
                .body(new ParameterizedTypeReference<List<RootFolder>>() {});

        if (folders == null || folders.isEmpty()) {
            throw new IllegalStateException("No root folder configured in Radarr");
        }
        if (folders.size() > 1) {
            log.warn("Multiple Radarr root folders configured, using the first: {}", folders.get(0).path());
        }
        return folders.get(0).path();
    }
}
