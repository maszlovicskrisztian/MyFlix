package com.maszlovicskrisztian.myflix_core.dtos;

public record PlaybackInfoDto(String mode, String url, long progressSeconds, Long durationSeconds) {
}
