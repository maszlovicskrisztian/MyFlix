package com.maszlovicskrisztian.myflix_core.service;

import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartupLibraryScanRunner implements CommandLineRunner {

    private final LibraryScanner libraryScanner;

    @Override
    public void run(String... args) throws Exception {
        libraryScanner.scan();
    }
}
