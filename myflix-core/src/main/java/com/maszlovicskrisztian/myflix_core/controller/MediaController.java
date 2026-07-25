package com.maszlovicskrisztian.myflix_core.controller;

import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import com.maszlovicskrisztian.myflix_core.repository.MediaItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/media")
public class MediaController {
    private final MediaItemRepository mediaItemRepository;

    @GetMapping
    public List<MediaItem> getAllMedia() {
        return mediaItemRepository.findAll();
    }
}
