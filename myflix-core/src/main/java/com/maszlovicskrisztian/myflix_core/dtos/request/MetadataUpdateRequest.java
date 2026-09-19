package com.maszlovicskrisztian.myflix_core.dtos.request;

import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Validated
public class MetadataUpdateRequest {
    private MediaType mediaType;
    private Long tmdbId;

    @NotBlank(message = "Title must be set!")
    private String title;
    private String overview;
    private String backdropPath;
    private String posterPath;
    private LocalDate releaseDate;
    private Integer runtimeMinutes;
    private List<String> genres;
    private Long showId;
    private Integer seasonNumber;
    private Integer episodeNumber;
}
