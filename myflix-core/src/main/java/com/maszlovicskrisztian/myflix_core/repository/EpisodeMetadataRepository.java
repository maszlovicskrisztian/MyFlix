package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.EpisodeMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EpisodeMetadataRepository extends JpaRepository<EpisodeMetadata, Long> {
    <T> List<T> findAllBy(Class<T> type);
}
