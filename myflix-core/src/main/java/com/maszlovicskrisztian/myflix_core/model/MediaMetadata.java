package com.maszlovicskrisztian.myflix_core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "media_metadata")
public class MediaMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_item_id", nullable = false, unique = true)
    private MediaItem mediaItem;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    private Long tmdbId;

    @Lob
    private String overview;
    private String title;

    private String posterPath;
    private String backdropPath;
    private LocalDate releaseDate;
    private Integer runtimeMinutes;

    private Integer seasonNumber;
    private Integer episodeNumber;

    @ElementCollection
    @CollectionTable(name = "media_metadata_genre", joinColumns = @JoinColumn(name = "media_metadata_id"))
    @Column(name = "genre_name")
    private List<String> genres = new ArrayList<>();
}
