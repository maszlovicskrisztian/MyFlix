package com.maszlovicskrisztian.myflix_core.tmdb.request;

public record TmdbSearchRequest(String title, String year, Integer season, Integer episode, String languageCode) {
}
