package com.maszlovicskrisztian.myflix_core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "file_info", uniqueConstraints = @UniqueConstraint(columnNames = {"relative_path"}))
@Getter
@Setter
@NoArgsConstructor
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String relativePath;
    private Long durationSeconds;
    private Integer resHeight;
    private String container;
    private String codec;
    private String audioCodec;
    private LocalDateTime addedAt;

    @OneToOne(mappedBy = "fileInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private MovieMetadata movieMetadata;

    @OneToOne(mappedBy = "fileInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private EpisodeMetadata episodeMetadata;

    @OneToMany(mappedBy = "fileInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WatchProgress> watchProgressEntries = new ArrayList<>();
}
