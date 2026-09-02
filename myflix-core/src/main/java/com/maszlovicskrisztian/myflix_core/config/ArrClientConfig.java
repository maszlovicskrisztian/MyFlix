package com.maszlovicskrisztian.myflix_core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ArrClientConfig {

    @Value("${NAS_HOST}")
    private String nasHost;

    @Value("${RADARR_PORT}")
    private String radarrPort;

    @Bean
    RestClient radarrClient(@Value("${RADARR_API_KEY}") String apiKey) {
        var baseUrl = "http://" + nasHost + ":" + radarrPort;
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }
}
