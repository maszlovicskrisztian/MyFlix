package com.maszlovicskrisztian.myflix_core.helpers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlResolver {
    @Value("${tmdb.images.base-url}")
    private String imagesBase;

    public String toImageUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }

        return imagesBase + "/original" + path;
    }
}
