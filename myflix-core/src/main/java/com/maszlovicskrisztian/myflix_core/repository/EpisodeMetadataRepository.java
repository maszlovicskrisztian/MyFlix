package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeMetadataRepository extends JpaRepository<EpisodeMetadata, Long> {
}
