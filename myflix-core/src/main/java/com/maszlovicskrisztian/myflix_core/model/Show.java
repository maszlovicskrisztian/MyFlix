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
@Table(name = "show", uniqueConstraints = @UniqueConstraint(columnNames = {"title", "first_air_date"}))
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tmdbId;

    @Lob
    private String overview;
    private String title;
    private String posterPath;
    private String backdropPath;
    private LocalDate firstAirDate;
    private Integer seasonCount;
    private Integer episodeCount;

    @ElementCollection
    @CollectionTable(name = "show_genre", joinColumns = @JoinColumn(name = "show_id"))
    @Column(name = "genre_name")
    private List<String> genres;

    @OneToMany(mappedBy = "show", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SeasonMetadata> seasons = new ArrayList<>();
}
