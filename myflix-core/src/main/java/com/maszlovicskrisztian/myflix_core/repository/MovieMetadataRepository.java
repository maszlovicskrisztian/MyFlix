package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovieMetadataRepository extends JpaRepository<MovieMetadata, Long> {
    <T> List<T> findAllBy(Class<T> type);
    Optional<MovieMetadata> findByTmdbId(Long tmdbId);
}
