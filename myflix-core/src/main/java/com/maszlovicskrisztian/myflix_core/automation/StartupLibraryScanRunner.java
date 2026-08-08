package com.maszlovicskrisztian.myflix_core.automation;

import com.maszlovicskrisztian.myflix_core.service.LibraryScanner;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StartupLibraryScanRunner implements CommandLineRunner {

    private final LibraryScanner libraryScanner;

    @Override
    public void run(String... args) throws Exception {
        libraryScanner.scanAndSave();
    }
}
