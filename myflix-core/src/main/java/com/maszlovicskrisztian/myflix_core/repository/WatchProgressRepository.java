package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.WatchProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {
    Optional<WatchProgress> findByProfileIdAndMediaItemId(Long profileId, Long mediaItemId);
}
