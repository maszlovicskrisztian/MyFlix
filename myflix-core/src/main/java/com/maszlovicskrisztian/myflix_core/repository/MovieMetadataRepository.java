package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.MovieMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieMetadataRepository extends JpaRepository<MovieMetadata, Long> {
}
