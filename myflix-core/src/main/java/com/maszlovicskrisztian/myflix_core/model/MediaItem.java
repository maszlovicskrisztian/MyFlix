package com.maszlovicskrisztian.myflix_core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "media_item")
@Getter
@Setter
@NoArgsConstructor
public class MediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String relativePath;
    private String fileName;
    private Long sizeBytes;
    private Long durationSeconds;
    private String container;
    private String codec;
    private String audioCodec;
    private LocalDateTime addedAt;

    @OneToOne(mappedBy = "mediaItem")
    private MediaMetadata metadata;

    @OneToMany(mappedBy = "mediaItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WatchProgress> watchProgressEntries = new ArrayList<>();
}
