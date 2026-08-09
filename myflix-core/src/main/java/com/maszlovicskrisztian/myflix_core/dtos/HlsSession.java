package com.maszlovicskrisztian.myflix_core.dtos;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public record HlsSession(Path playlistPath, Process process, AtomicReference<Instant> lastAccessed, long startSeconds) {
}
