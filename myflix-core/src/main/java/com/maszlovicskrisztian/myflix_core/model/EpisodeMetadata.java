package com.maszlovicskrisztian.myflix_core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "episode_metadata")
public class EpisodeMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tmdbId;

    @Lob
    private String overview;
    private String title;
    private String stillPath;
    private LocalDate releaseDate;
    private Integer runtimeMinutes;
    private Integer seasonNumber;
    private Integer episodeNumber;

    private String seasonTitle;

    @Lob
    private String seasonOverview;
    private String seasonPosterPath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_info_id", nullable = false, unique = true)
    private FileInfo fileInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id")
    private Show show;
}