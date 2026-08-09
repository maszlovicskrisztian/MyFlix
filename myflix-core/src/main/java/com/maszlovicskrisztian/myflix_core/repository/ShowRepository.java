package com.maszlovicskrisztian.myflix_core.repository;

import com.maszlovicskrisztian.myflix_core.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {
    Optional<Show> findByTmdbId(Long tmdbId);
}
