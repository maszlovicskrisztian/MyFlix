package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.MediaMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaMetadataRepository extends JpaRepository<MediaMetadata, Long> {
}
