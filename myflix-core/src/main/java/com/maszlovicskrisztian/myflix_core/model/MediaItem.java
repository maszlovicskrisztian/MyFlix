package com.maszlovicskrisztian.myflix_core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
    private LocalDateTime addedAt;
}
