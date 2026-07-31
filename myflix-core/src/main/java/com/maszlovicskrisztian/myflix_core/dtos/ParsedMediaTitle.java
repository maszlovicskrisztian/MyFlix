package com.maszlovicskrisztian.myflix_core.dtos;

public record ParsedMediaTitle (String title, Integer year, boolean isSeries, Integer season, Integer episode) {
}
