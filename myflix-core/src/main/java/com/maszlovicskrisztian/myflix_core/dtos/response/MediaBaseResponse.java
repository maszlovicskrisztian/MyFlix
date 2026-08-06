package com.maszlovicskrisztian.myflix_core.dtos.response;

public record MediaBaseResponse(
        Long showId,
        Long fileInfoId,
        String title,
        String imagePath) {
}
