package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.dtos.enums.MediaType;
import com.maszlovicskrisztian.myflix_core.model.MetadataTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetadataTranslationRepository extends JpaRepository<MetadataTranslation, Long> {
    Optional<MetadataTranslation> findByEntityTypeAndEntityIdAndLanguageCode(MediaType entityType, Long entityId, String languageCode);
}
