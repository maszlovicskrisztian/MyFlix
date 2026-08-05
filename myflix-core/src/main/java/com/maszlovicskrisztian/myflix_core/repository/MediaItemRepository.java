package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {
    <T> Optional<T> findById(Long id, Class<T> type);
}
