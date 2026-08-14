package com.maszlovicskrisztian.myflix_core.dtos.response;

import java.util.List;

public record MediaBaseResponse(
        Long showId,
        Long fileInfoId,
        String title,
        String imagePath,
        List<String> genres) {
}
