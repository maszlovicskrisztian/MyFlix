package com.maszlovicskrisztian.myflix_core.helpers;

import com.maszlovicskrisztian.myflix_core.tmdb.request.TmdbSearchRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class MediaTitleParser {

    private static final Pattern YEAR = Pattern.compile("\\b(19|20)\\d{2}\\b");
    private static final Pattern EPISODE = Pattern.compile("(?i)e(\\d{1,3})");
    private static final Pattern SEASON = Pattern.compile("(?i)s(\\d{1,2})");

    public static TmdbSearchRequest getSearchDetailsFromPath(Path relativePath) {
        log.info("Resolving data from {}", relativePath);

        String title = getTitle(relativePath);
        log.info("Title resolved to: {}", title);

        String year = getYear(relativePath);
        if (year != null)
            log.info("Release date resolved to: {}", year);
        else
            log.info("Release date could not be resolved");

        Integer season = getSeason(relativePath);
        if (season != null)
            log.info("Season resolved to: {}", season);
        else
            log.info("Season could not be resolved");

        Integer episode = getEpisode(relativePath);
        if (episode != null)
            log.info("Episode resolved to: {}", episode);
        else
            log.info("Episode could not be resolved");

        return new TmdbSearchRequest(title, year, season, episode, "en");
    }

    private static String getTitle(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();

        log.debug("Started title parsing from folder name: {}", folderName);

        String titlePart = tryResolveTitle(folderName);

        if (titlePart != null) {
            return titlePart;
        }

        log.debug("Could not get title from folder, trying with file name.");

        String fileName = relativePath.getFileName().toString();
        if (FileHelper.hasVideoExtension(relativePath.getFileName()))
            fileName = FileHelper.stripExtension(fileName);

        titlePart = tryResolveTitle(fileName);

        if (titlePart != null) {
            return titlePart;
        }

        log.debug("Could not get title from filename.");
        return fileName
                .replace('.', ' ')
                .replace('_', ' ')
                .trim();
    }

    private static String tryResolveTitle(String path) {
        String titlePart = null;

        Matcher seasonMatcher = SEASON.matcher(path);
        if (seasonMatcher.find()) {
            titlePart = path.substring(0, seasonMatcher.start());
        }

        String toBeMatched = titlePart == null ? path : titlePart;
        Matcher yearMatcher = YEAR.matcher(toBeMatched);
        if (yearMatcher.find()) {
            titlePart = toBeMatched.substring(0, yearMatcher.start());
        }

        if (titlePart == null)
            return null;

        return titlePart
                .replace('.', ' ')
                .replace('_', ' ')
                .trim();
    }

    private static String getYear(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();

        String year = tryResolveYear(folderName);
        if (year != null){
            return year;
        }

        log.debug("Could not get release date from folder, trying with file name.");

        String fileName = relativePath.getFileName().toString();
        year = tryResolveYear(fileName);
        if (year != null){
            return year;
        }

        log.debug("Could not get release date from filename.");
        return null;
    }

    private static String tryResolveYear(String path) {
        Matcher yearMatcher = YEAR.matcher(path);
        if (yearMatcher.find()) {
            return yearMatcher.group();
        }

        return null;
    }

    private static Integer getSeason(Path relativePath) {
        String folderName = relativePath.getName(relativePath.getNameCount() - 2).toString();
        Matcher seasonMatcher = SEASON.matcher(folderName);

        if (!seasonMatcher.find()) {
            return null;
        }

        return Integer.parseInt(seasonMatcher.group(1));
    }

    private static Integer getEpisode(Path relativePath) {
        String fileName = relativePath.getFileName().toString();
        Matcher episodeMatcher = EPISODE.matcher(fileName);

        if (!episodeMatcher.find()) {
            return null;
        }

        return  Integer.parseInt(episodeMatcher.group(1));
    }
}
