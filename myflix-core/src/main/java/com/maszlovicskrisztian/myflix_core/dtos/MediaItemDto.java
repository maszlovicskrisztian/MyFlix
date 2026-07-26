package com.maszlovicskrisztian.myflix_core.dtos;

import java.time.LocalDateTime;

public record MediaItemDto(
        Long id,
        String title,
        Long durationSeconds,
        LocalDateTime addedAt
) {}
