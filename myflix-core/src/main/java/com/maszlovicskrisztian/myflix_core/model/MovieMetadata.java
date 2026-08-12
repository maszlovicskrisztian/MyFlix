package com.maszlovicskrisztian.myflix_core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "movie_metadata", uniqueConstraints = @UniqueConstraint(columnNames = {"title", "release_date"}))
public class MovieMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tmdbId;

    @Lob
    private String overview;
    private String title;
    private String posterPath;
    private String backdropPath;
    private LocalDate releaseDate;
    private Integer runtimeMinutes;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_info_id", nullable = false, unique = true)
    private FileInfo fileInfo;

    @ElementCollection
    @CollectionTable(name = "movie_metadata_genre", joinColumns = @JoinColumn(name = "movie_metadata_id"))
    @Column(name = "genre_name")
    private List<String> genres;
}
