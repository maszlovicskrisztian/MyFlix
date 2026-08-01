package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.MediaItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaItemRepository extends JpaRepository<MediaItem, Long> {
}
