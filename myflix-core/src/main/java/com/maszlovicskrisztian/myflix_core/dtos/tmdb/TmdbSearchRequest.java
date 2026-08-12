package com.maszlovicskrisztian.myflix_core.dtos.tmdb;

public record TmdbSearchRequest(String title, String year, Integer season, Integer episode) {
}
