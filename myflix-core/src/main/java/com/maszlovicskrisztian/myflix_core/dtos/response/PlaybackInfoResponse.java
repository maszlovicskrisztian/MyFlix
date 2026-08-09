package com.maszlovicskrisztian.myflix_core.dtos.response;

public record PlaybackInfoResponse(String mode, String url, long progressSeconds, Long durationSeconds) {
}
