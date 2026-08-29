package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.SeasonMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonMetadataRepository extends JpaRepository<SeasonMetadata, Long> {
    Optional<SeasonMetadata> findByShowIdAndSeasonNumber(Long showId, Integer seasonNumber);
}
