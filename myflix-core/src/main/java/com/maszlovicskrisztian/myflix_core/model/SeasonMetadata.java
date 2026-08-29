package com.maszlovicskrisztian.myflix_core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "season_metadata", uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seasonNumber"}))
public class SeasonMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tmdbId;

    @Lob
    private String overview;
    private String title;
    private String posterPath;
    private LocalDate releaseDate;
    private Integer seasonNumber;
    private Integer episodeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id")
    private Show show;

    @OneToMany(mappedBy = "season", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EpisodeMetadata> episodes = new ArrayList<>();
}
