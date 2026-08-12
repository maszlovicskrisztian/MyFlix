package com.maszlovicskrisztian.myflix_core.dtos;

public record MediaProbeResult(String videoCodec, String audioCodec, String container, Long durationSeconds, Integer resHeight) {
}
